package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.purchase.dto.CreateSupplierPaymentRequest;
import com.erp.platform.modules.purchase.dto.SupplierPaymentDto;
import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.entity.SupplierAdvance.AdvanceStatus;
import com.erp.platform.modules.purchase.entity.SupplierPayment;
import com.erp.platform.modules.purchase.entity.SupplierPayment.PayStatus;
import com.erp.platform.modules.purchase.entity.SupplierPaymentAllocation;
import com.erp.platform.modules.purchase.repository.SupplierAdvanceRepository;
import com.erp.platform.modules.purchase.repository.SupplierPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierAdvanceRepository supplierAdvanceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<SupplierPaymentDto> list(UUID vendorId, PayStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<SupplierPayment> page;
        if (vendorId != null && status != null) {
            page = supplierPaymentRepository.findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(tenantId, vendorId, status, pageable);
        } else if (vendorId != null) {
            page = supplierPaymentRepository.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable);
        } else if (status != null) {
            page = supplierPaymentRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = supplierPaymentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public SupplierPaymentDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SupplierPaymentDto create(CreateSupplierPaymentRequest request) {
        UUID tenantId = tenantContext.current();
        SupplierPayment payment = new SupplierPayment();
        payment.setTenantId(tenantId);
        payment.setPaymentNumber(generatePaymentNumber(tenantId));
        payment.setVendorId(request.getVendorId());
        payment.setVendorName(request.getVendorName());
        payment.setPurchaseInvoiceId(request.getPurchaseInvoiceId());
        payment.setInvoiceNumber(request.getInvoiceNumber());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        payment.setExchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE);
        payment.setBankAccountId(request.getBankAccountId());
        payment.setBankAccountName(request.getBankAccountName());
        payment.setReferenceNumber(request.getReferenceNumber());
        BigDecimal tdsAmount = request.getTdsAmount() != null ? request.getTdsAmount() : BigDecimal.ZERO;
        BigDecimal tdsPercent = request.getTdsPercent() != null ? request.getTdsPercent() : BigDecimal.ZERO;
        payment.setTdsAmount(tdsAmount);
        payment.setTdsPercent(tdsPercent);
        payment.setNetPayment(payment.getAmount().subtract(tdsAmount));
        payment.setNotes(request.getNotes());
        payment.setStatus(PayStatus.DRAFT);

        SupplierPayment saved = supplierPaymentRepository.save(payment);

        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            List<SupplierPaymentAllocation> allocations = new ArrayList<>();
            for (CreateSupplierPaymentRequest.AllocationRequest ar : request.getAllocations()) {
                SupplierPaymentAllocation alloc = new SupplierPaymentAllocation();
                alloc.setTenantId(tenantId);
                alloc.setPayment(saved);
                alloc.setPurchaseInvoiceId(ar.getInvoiceId());
                alloc.setInvoiceNumber(ar.getInvoiceNumber());
                alloc.setInvoiceAmount(ar.getInvoiceAmount());
                alloc.setAllocatedAmount(ar.getAllocatedAmount());
                BigDecimal outstanding = ar.getInvoiceAmount() != null && ar.getAllocatedAmount() != null
                        ? ar.getInvoiceAmount().subtract(ar.getAllocatedAmount())
                        : BigDecimal.ZERO;
                alloc.setOutstandingAfter(outstanding);
                allocations.add(alloc);
            }
            saved.setAllocations(allocations);
            saved = supplierPaymentRepository.save(saved);
        }

        log.info("SupplierPayment created: id={}, number={}", saved.getId(), saved.getPaymentNumber());
        return toDto(saved);
    }

    @Transactional
    public SupplierPaymentDto update(UUID id, CreateSupplierPaymentRequest request) {
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT payments can be edited");
        }
        payment.setVendorId(request.getVendorId());
        payment.setVendorName(request.getVendorName());
        payment.setPurchaseInvoiceId(request.getPurchaseInvoiceId());
        payment.setInvoiceNumber(request.getInvoiceNumber());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        payment.setExchangeRate(request.getExchangeRate() != null ? request.getExchangeRate() : BigDecimal.ONE);
        payment.setBankAccountId(request.getBankAccountId());
        payment.setBankAccountName(request.getBankAccountName());
        payment.setReferenceNumber(request.getReferenceNumber());
        BigDecimal tdsAmount = request.getTdsAmount() != null ? request.getTdsAmount() : BigDecimal.ZERO;
        BigDecimal tdsPercent = request.getTdsPercent() != null ? request.getTdsPercent() : BigDecimal.ZERO;
        payment.setTdsAmount(tdsAmount);
        payment.setTdsPercent(tdsPercent);
        payment.setNetPayment(payment.getAmount().subtract(tdsAmount));
        payment.setNotes(request.getNotes());
        log.info("SupplierPayment updated: id={}", id);
        return toDto(supplierPaymentRepository.save(payment));
    }

    @Transactional
    public SupplierPaymentDto approve(UUID id, String approvedBy) {
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.PENDING_APPROVAL) {
            throw AppException.badRequest("Only PENDING_APPROVAL payments can be approved");
        }
        payment.setStatus(PayStatus.APPROVED);
        payment.setApprovedBy(approvedBy);
        payment.setApprovedAt(LocalDateTime.now());
        return toDto(supplierPaymentRepository.save(payment));
    }

    @Transactional
    public SupplierPaymentDto process(UUID id) {
        UUID tenantId = tenantContext.current();
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.APPROVED) {
            throw AppException.badRequest("Only APPROVED payments can be processed");
        }

        // Auto-deduct available supplier advances for this vendor
        if (payment.getVendorId() != null) {
            List<SupplierAdvance> advances = supplierAdvanceRepository
                    .findByTenantIdAndVendorIdAndStatusNotAndDeletedAtIsNull(tenantId, payment.getVendorId(), AdvanceStatus.CANCELLED);
            BigDecimal advanceApplied = BigDecimal.ZERO;
            for (SupplierAdvance adv : advances) {
                if (adv.getStatus() == AdvanceStatus.FULLY_ADJUSTED) continue;
                BigDecimal adjusted = adv.getAdjustedAmount() != null ? adv.getAdjustedAmount() : BigDecimal.ZERO;
                BigDecimal available = adv.getAmount().subtract(adjusted);
                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;
                BigDecimal remaining = payment.getAmount().subtract(advanceApplied);
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal apply = available.min(remaining);
                adv.setAdjustedAmount(adjusted.add(apply));
                adv.setStatus(adv.getAdjustedAmount().compareTo(adv.getAmount()) >= 0
                        ? AdvanceStatus.FULLY_ADJUSTED : AdvanceStatus.PARTIALLY_ADJUSTED);
                supplierAdvanceRepository.save(adv);
                advanceApplied = advanceApplied.add(apply);
            }
            if (advanceApplied.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal net = payment.getNetPayment() != null ? payment.getNetPayment() : payment.getAmount();
                payment.setNetPayment(net.subtract(advanceApplied).max(BigDecimal.ZERO));
                log.info("Applied supplier advance {} to payment {}", advanceApplied, payment.getPaymentNumber());
            }
        }

        payment.setStatus(PayStatus.PROCESSED);
        payment.setProcessedAt(LocalDateTime.now());
        SupplierPayment saved = supplierPaymentRepository.save(payment);

        BigDecimal payAmount = saved.getNetPayment() != null ? saved.getNetPayment() : saved.getAmount();
        if (payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                postPaymentJournal(tenantId, saved, payAmount);
            } catch (Exception e) {
                log.warn("GL entry skipped for supplier payment {}: {}", saved.getPaymentNumber(), e.getMessage());
            }
        }
        return toDto(saved);
    }

    private void postPaymentJournal(UUID tenantId, SupplierPayment payment, BigDecimal amount) {
        List<Account> apAccts   = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_PAYABLE");
        List<Account> bankAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK");
        if (apAccts.isEmpty() || bankAccts.isEmpty()) return;

        BigDecimal net = amount.setScale(2, RoundingMode.HALF_UP);
        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setEntryDate(payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now());
        je.setDescription("Supplier payment: " + payment.getPaymentNumber()
                + (payment.getVendorName() != null ? " – " + payment.getVendorName() : ""));
        je.setStatus(JEStatus.DRAFT);
        je.setReferenceType("SUPPLIER_PAYMENT");
        je.setReferenceId(payment.getId());
        je.setReferenceNumber(payment.getPaymentNumber());

        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccountId(apAccts.get(0).getId());
        dr.setDebitAmount(net);
        dr.setCreditAmount(BigDecimal.ZERO);

        JournalEntryLine cr = new JournalEntryLine();
        cr.setAccountId(bankAccts.get(0).getId());
        cr.setDebitAmount(BigDecimal.ZERO);
        cr.setCreditAmount(net);

        je.setLines(new java.util.ArrayList<>(java.util.List.of(dr, cr)));
        journalEntryService.create(je);
        log.info("GL entry posted for supplier payment {}: DR AP / CR BANK {}", payment.getPaymentNumber(), net);
    }

    @Transactional
    public SupplierPaymentDto reconcile(UUID id) {
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.PROCESSED) {
            throw AppException.badRequest("Only PROCESSED payments can be reconciled");
        }
        payment.setStatus(PayStatus.RECONCILED);
        return toDto(supplierPaymentRepository.save(payment));
    }

    @Transactional
    public SupplierPaymentDto cancel(UUID id) {
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.DRAFT && payment.getStatus() != PayStatus.PENDING_APPROVAL) {
            throw AppException.badRequest("Only DRAFT or PENDING_APPROVAL payments can be cancelled");
        }
        payment.setStatus(PayStatus.CANCELLED);
        return toDto(supplierPaymentRepository.save(payment));
    }

    @Transactional
    public void delete(UUID id) {
        SupplierPayment payment = findOrThrow(id);
        if (payment.getStatus() != PayStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT payments can be deleted");
        }
        payment.setDeletedAt(LocalDateTime.now());
        supplierPaymentRepository.save(payment);
        log.info("SupplierPayment soft-deleted: id={}", id);
    }

    private SupplierPayment findOrThrow(UUID id) {
        return supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Supplier payment not found: " + id));
    }

    private String generatePaymentNumber(UUID tenantId) {
        long count = supplierPaymentRepository.countByTenantId(tenantId);
        return String.format("SPAY-%d-%03d", Year.now().getValue(), count + 1);
    }

    private SupplierPaymentDto toDto(SupplierPayment p) {
        SupplierPaymentDto dto = new SupplierPaymentDto();
        dto.setId(p.getId());
        dto.setPaymentNumber(p.getPaymentNumber());
        dto.setVendorId(p.getVendorId());
        dto.setVendorName(p.getVendorName());
        dto.setPurchaseInvoiceId(p.getPurchaseInvoiceId());
        dto.setInvoiceNumber(p.getInvoiceNumber());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setExchangeRate(p.getExchangeRate());
        dto.setBankAccountId(p.getBankAccountId());
        dto.setBankAccountName(p.getBankAccountName());
        dto.setReferenceNumber(p.getReferenceNumber());
        dto.setTdsAmount(p.getTdsAmount());
        dto.setTdsPercent(p.getTdsPercent());
        dto.setNetPayment(p.getNetPayment());
        dto.setStatus(p.getStatus());
        dto.setApprovedBy(p.getApprovedBy());
        dto.setApprovedAt(p.getApprovedAt());
        dto.setProcessedAt(p.getProcessedAt());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());
        if (p.getAllocations() != null) {
            dto.setAllocations(p.getAllocations().stream().map(a -> {
                SupplierPaymentDto.AllocationDto ad = new SupplierPaymentDto.AllocationDto();
                ad.setId(a.getId());
                ad.setPurchaseInvoiceId(a.getPurchaseInvoiceId());
                ad.setInvoiceNumber(a.getInvoiceNumber());
                ad.setInvoiceAmount(a.getInvoiceAmount());
                ad.setAllocatedAmount(a.getAllocatedAmount());
                ad.setOutstandingAfter(a.getOutstandingAfter());
                return ad;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
