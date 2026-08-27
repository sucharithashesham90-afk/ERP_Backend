package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.dto.CreatePurchaseInvoiceRequest;
import com.erp.platform.modules.purchase.dto.PurchaseInvoiceDto;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository piRepository;
    private final VendorRepository vendorRepository;
    private final AccountRepository accountRepository;
    private final GoodsReceiptRepository grnRepository;
    private final JournalEntryService journalEntryService;
    private final PartyLedgerService partyLedgerService;
    private final TenantContext tenantContext;

    public PurchaseInvoiceService(PurchaseInvoiceRepository piRepository,
                                  VendorRepository vendorRepository,
                                  AccountRepository accountRepository,
                                  GoodsReceiptRepository grnRepository,
                                  @Lazy JournalEntryService journalEntryService,
                                  PartyLedgerService partyLedgerService,
                                  TenantContext tenantContext) {
        this.piRepository = piRepository;
        this.vendorRepository = vendorRepository;
        this.accountRepository = accountRepository;
        this.grnRepository = grnRepository;
        this.journalEntryService = journalEntryService;
        this.partyLedgerService = partyLedgerService;
        this.tenantContext = tenantContext;
    }

    public PageResponse<PurchaseInvoiceDto> list(PIStatus status, UUID vendorId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = vendorId != null
                ? piRepository.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable)
                : status != null
                    ? piRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                    : piRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public PurchaseInvoiceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PurchaseInvoiceDto create(CreatePurchaseInvoiceRequest request) {
        UUID tenantId = tenantContext.current();

        if (request.getPurchaseOrderId() != null) {
            piRepository.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(tenantId, request.getPurchaseOrderId())
                    .stream()
                    .filter(pi -> pi.getStatus() != PIStatus.CANCELLED)
                    .findFirst()
                    .ifPresent(existing -> {
                        throw AppException.badRequest(
                                "Invoice " + existing.getPiNumber() + " already exists for this Purchase Order");
                    });
        }

        Vendor vendor = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getVendorId())
                .orElseThrow(() -> AppException.notFound("Vendor not found: " + request.getVendorId()));

        PurchaseInvoice pi = new PurchaseInvoice();
        pi.setTenantId(tenantId);
        pi.setVendorId(vendor.getId());
        pi.setVendorName(vendor.getName());
        pi.setPiNumber(generatePINumber());
        pi.setVendorInvoiceNumber(request.getVendorInvoiceNumber());
        pi.setPurchaseOrderId(request.getPurchaseOrderId());
        pi.setGoodsReceiptId(request.getGoodsReceiptId());
        pi.setStatus(PIStatus.DRAFT);
        pi.setInvoiceDate(request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDate.now());
        pi.setDueDate(request.getDueDate());
        pi.setPaymentTerms(request.getPaymentTerms());
        pi.setNotes(request.getNotes());

        pi.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO);
        pi.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        pi.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        pi.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO);
        pi.setExciseDuty(request.getExciseDuty() != null ? request.getExciseDuty() : BigDecimal.ZERO);
        pi.setPfCharge(request.getPfCharge() != null ? request.getPfCharge() : BigDecimal.ZERO);

        pi.setTdsApplicable(request.isTdsApplicable());
        pi.setTdsSection(request.getTdsSection());
        pi.setTdsPercent(request.getTdsPercent() != null ? request.getTdsPercent() : BigDecimal.ZERO);

        if (request.getItems() != null) {
            try {
                pi.setItemsJson(new ObjectMapper().writeValueAsString(request.getItems()));
            } catch (Exception e) {
                log.error("Error serializing purchase invoice items", e);
            }
        }

        calculateTotals(pi);

        pi.setPaidAmount(BigDecimal.ZERO);
        pi.setBalanceDue(pi.getNetPayable());

        PurchaseInvoice saved = piRepository.save(pi);

        // Track vendor outstanding: new PI increases what we owe
        vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, saved.getVendorId()).ifPresent(v -> {
            BigDecimal current = v.getOutstandingBalance() != null ? v.getOutstandingBalance() : BigDecimal.ZERO;
            v.setOutstandingBalance(current.add(saved.getNetPayable()));
            vendorRepository.save(v);
        });

        log.info("Purchase Invoice created: id={}, number={}", saved.getId(), saved.getPiNumber());
        return toDto(saved);
    }

    @Transactional
    public PurchaseInvoiceDto update(UUID id, CreatePurchaseInvoiceRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseInvoice pi = findOrThrow(id);

        Vendor vendor = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getVendorId())
                .orElseThrow(() -> AppException.notFound("Vendor not found: " + request.getVendorId()));

        pi.setVendorId(vendor.getId());
        pi.setVendorName(vendor.getName());
        pi.setVendorInvoiceNumber(request.getVendorInvoiceNumber());
        if (request.getPurchaseOrderId() != null) pi.setPurchaseOrderId(request.getPurchaseOrderId());
        if (request.getGoodsReceiptId() != null) pi.setGoodsReceiptId(request.getGoodsReceiptId());
        if (request.getInvoiceDate() != null) pi.setInvoiceDate(request.getInvoiceDate());
        if (request.getDueDate() != null) pi.setDueDate(request.getDueDate());
        pi.setPaymentTerms(request.getPaymentTerms());
        pi.setNotes(request.getNotes());
        pi.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO);
        pi.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        pi.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        pi.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO);
        pi.setExciseDuty(request.getExciseDuty() != null ? request.getExciseDuty() : BigDecimal.ZERO);
        pi.setPfCharge(request.getPfCharge() != null ? request.getPfCharge() : BigDecimal.ZERO);
        pi.setTdsApplicable(request.isTdsApplicable());
        pi.setTdsSection(request.getTdsSection());
        pi.setTdsPercent(request.getTdsPercent() != null ? request.getTdsPercent() : BigDecimal.ZERO);

        if (request.getItems() != null) {
            try {
                pi.setItemsJson(new ObjectMapper().writeValueAsString(request.getItems()));
            } catch (Exception e) {
                log.error("Error serializing purchase invoice items on update", e);
            }
        }

        calculateTotals(pi);
        pi.setBalanceDue(pi.getNetPayable().subtract(pi.getPaidAmount()));

        PurchaseInvoice saved = piRepository.save(pi);
        log.info("Purchase Invoice updated: id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public PurchaseInvoiceDto recordPayment(UUID id, BigDecimal amount) {
        PurchaseInvoice pi = findOrThrow(id);
        if (pi.getStatus() == PIStatus.CANCELLED) {
            throw AppException.badRequest("Cannot record payment on cancelled purchase invoice");
        }
        BigDecimal newPaid = pi.getPaidAmount().add(amount);
        if (newPaid.compareTo(pi.getNetPayable()) > 0) {
            throw AppException.badRequest("Payment amount exceeds net payable");
        }
        pi.setPaidAmount(newPaid);
        pi.setBalanceDue(pi.getNetPayable().subtract(newPaid));
        boolean fullyPaid = pi.getBalanceDue().compareTo(BigDecimal.ZERO) == 0;
        pi.setStatus(fullyPaid ? PIStatus.PAID : PIStatus.PARTIALLY_PAID);
        PurchaseInvoice saved = piRepository.save(pi);

        // When fully paid, clear vendor outstanding balance for this invoice
        if (fullyPaid) {
            UUID tenantId = tenantContext.current();
            vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, saved.getVendorId()).ifPresent(v -> {
                BigDecimal current = v.getOutstandingBalance() != null ? v.getOutstandingBalance() : BigDecimal.ZERO;
                v.setOutstandingBalance(current.subtract(saved.getNetPayable()).max(BigDecimal.ZERO));
                vendorRepository.save(v);
            });
        }
        return toDto(saved);
    }

    @Transactional
    public PurchaseInvoiceDto approve(UUID id) {
        UUID tenantId = tenantContext.current();
        PurchaseInvoice pi = findOrThrow(id);
        if (pi.getStatus() != PIStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT purchase invoices can be approved");
        }
        pi.setStatus(PIStatus.APPROVED);
        PurchaseInvoice saved = piRepository.save(pi);

        // Update vendor outstanding balance
        if (saved.getVendorId() != null) {
            vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, saved.getVendorId()).ifPresent(v -> {
                BigDecimal current = v.getOutstandingBalance() != null ? v.getOutstandingBalance() : BigDecimal.ZERO;
                v.setOutstandingBalance(current.add(saved.getNetPayable()));
                vendorRepository.save(v);
            });
        }

        // Create & post AP journal entry directly linked to specific Supplier Ledger account
        try {
            String vName = (saved.getVendorName() != null && !saved.getVendorName().isBlank()) ? saved.getVendorName() : "Supplier";
            // Post to the vendor's own party ledger, resolved by its stable id-derived code.
            // Matching on name instead used to spawn ungrouped "ghost" accounts that never
            // appeared under Sundry Creditors, so the invoice was missing from the supplier
            // ledger search.
            Account vendorAcct = null;
            if (saved.getVendorId() != null) {
                partyLedgerService.ensureLedger(PartyLedgerService.PartyType.VENDOR, vName, saved.getVendorId(), null);
                String vendorLedgerCode = partyLedgerService.ledgerCode(
                        PartyLedgerService.PartyType.VENDOR, saved.getVendorId());
                vendorAcct = accountRepository
                        .findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, vendorLedgerCode)
                        .orElse(null);
            }
            if (vendorAcct == null) {
                vendorAcct = accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, vName)
                        .orElseGet(() -> {
                            Account a = new Account();
                            a.setTenantId(tenantId);
                            a.setCode("VEND-" + (saved.getVendorId() != null ? saved.getVendorId().toString().substring(0, 8).toUpperCase() : "ACCOUNT"));
                            a.setName(vName);
                            a.setType("LIABILITY");
                            a.setSubType("ACCOUNTS_PAYABLE");
                            a.setActive(true);
                            return accountRepository.save(a);
                        });
            }

            Account expAcct = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "PURCHASE_EXPENSE")
                    .stream().findFirst().orElseGet(() -> {
                        Account a = new Account();
                        a.setTenantId(tenantId);
                        a.setCode("EXP-PURCHASE");
                        a.setName("Purchase Expense Account");
                        a.setType("EXPENSE");
                        a.setSubType("PURCHASE_EXPENSE");
                        a.setActive(true);
                        return accountRepository.save(a);
                    });

            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setEntryDate(saved.getInvoiceDate() != null ? saved.getInvoiceDate() : LocalDate.now());
            je.setDescription("Purchase Invoice " + saved.getPiNumber() + " — " + vName);
            je.setReferenceType("PURCHASE_INVOICE");
            je.setReferenceId(saved.getId());
            je.setReferenceNumber(saved.getPiNumber());

            JournalEntryLine debit = new JournalEntryLine();
            debit.setAccountId(expAcct.getId());
            debit.setAccountCode(expAcct.getCode());
            debit.setAccountName(expAcct.getName());
            debit.setDebitAmount(saved.getNetPayable());
            debit.setCreditAmount(BigDecimal.ZERO);
            debit.setDescription("Purchase expense — " + saved.getPiNumber());

            JournalEntryLine credit = new JournalEntryLine();
            credit.setAccountId(vendorAcct.getId());
            credit.setAccountCode(vendorAcct.getCode());
            credit.setAccountName(vendorAcct.getName());
            credit.setDebitAmount(BigDecimal.ZERO);
            credit.setCreditAmount(saved.getNetPayable());
            credit.setDescription("Accounts payable — " + vName + " (" + saved.getPiNumber() + ")");

            je.setLines(new java.util.ArrayList<>(List.of(debit, credit)));
            JournalEntry createdJe = journalEntryService.create(je);
            journalEntryService.post(createdJe.getId());
        } catch (Exception e) {
            log.warn("Could not post AP journal for PI {}: {}", saved.getPiNumber(), e.getMessage());
        }

        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        PurchaseInvoice pi = findOrThrow(id);
        pi.setDeletedAt(LocalDateTime.now());
        piRepository.save(pi);
        log.info("Purchase Invoice soft-deleted: id={}", id);
    }

    private void calculateTotals(PurchaseInvoice pi) {
        BigDecimal gross = pi.getSubtotal()
                .add(pi.getTaxAmount())
                .subtract(pi.getDiscountAmount())
                .add(pi.getFreightCharges())
                .add(pi.getExciseDuty() != null ? pi.getExciseDuty() : BigDecimal.ZERO)
                .add(pi.getPfCharge() != null ? pi.getPfCharge() : BigDecimal.ZERO);
        pi.setTotalAmount(gross);

        // TDS is deducted from the payable amount (vendor receives less; TDS remitted to govt)
        if (pi.isTdsApplicable() && pi.getTdsPercent().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tds = pi.getSubtotal()
                    .multiply(pi.getTdsPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            pi.setTdsAmount(tds);
        } else {
            pi.setTdsAmount(BigDecimal.ZERO);
        }
        pi.setNetPayable(pi.getTotalAmount().subtract(pi.getTdsAmount()));
    }

    private PurchaseInvoiceDto toDto(PurchaseInvoice pi) {
        PurchaseInvoiceDto dto = new PurchaseInvoiceDto();
        dto.setId(pi.getId());
        dto.setTenantId(pi.getTenantId());
        dto.setPiNumber(pi.getPiNumber());
        dto.setVendorInvoiceNumber(pi.getVendorInvoiceNumber());
        dto.setVendorId(pi.getVendorId());
        dto.setVendorName(pi.getVendorName());
        dto.setPurchaseOrderId(pi.getPurchaseOrderId());
        
        UUID grnId = pi.getGoodsReceiptId();
        if (grnId == null && pi.getPurchaseOrderId() != null && grnRepository != null) {
            try {
                var grns = grnRepository.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(pi.getTenantId(), pi.getPurchaseOrderId());
                if (!grns.isEmpty()) {
                    grnId = grns.get(0).getId();
                }
            } catch (Exception ignored) {}
        }
        dto.setGoodsReceiptId(grnId);
        dto.setInvoiceDate(pi.getInvoiceDate());
        dto.setDueDate(pi.getDueDate());
        dto.setStatus(pi.getStatus());
        dto.setSubtotal(pi.getSubtotal());
        dto.setTaxAmount(pi.getTaxAmount());
        dto.setDiscountAmount(pi.getDiscountAmount());
        dto.setFreightCharges(pi.getFreightCharges());
        dto.setExciseDuty(pi.getExciseDuty());
        dto.setPfCharge(pi.getPfCharge());
        dto.setTdsApplicable(pi.isTdsApplicable());
        dto.setTdsSection(pi.getTdsSection());
        dto.setTdsPercent(pi.getTdsPercent());
        dto.setTdsAmount(pi.getTdsAmount());
        dto.setTotalAmount(pi.getTotalAmount());
        dto.setNetPayable(pi.getNetPayable());
        dto.setPaidAmount(pi.getPaidAmount());
        dto.setBalanceDue(pi.getBalanceDue());
        dto.setPaymentTerms(pi.getPaymentTerms());
        dto.setNotes(pi.getNotes());
        if (pi.getItemsJson() != null && !pi.getItemsJson().trim().isEmpty()) {
            try {
                dto.setItems(new ObjectMapper().readValue(pi.getItemsJson(), new TypeReference<List<Map<String, Object>>>() {}));
            } catch (Exception e) {
                log.error("Error deserializing purchase invoice items", e);
            }
        }
        dto.setCreatedAt(pi.getCreatedAt());
        return dto;
    }

    private PurchaseInvoice findOrThrow(UUID id) {
        return piRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Purchase invoice not found: " + id));
    }

    private String generatePINumber() {
        return "PI-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
