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
import com.erp.platform.modules.purchase.entity.AdvanceAdjustment;
import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.entity.SupplierAdvance.AdvanceStatus;
import com.erp.platform.modules.purchase.repository.AdvanceAdjustmentRepository;
import com.erp.platform.modules.purchase.repository.SupplierAdvanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierAdvanceService {

    private final SupplierAdvanceRepository repo;
    private final AdvanceAdjustmentRepository adjustmentRepo;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<SupplierAdvance> list(UUID vendorId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (vendorId != null) {
            return PageResponse.of(repo.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable));
        }
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public SupplierAdvance getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public SupplierAdvance create(SupplierAdvance req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);
        req.setStatus(AdvanceStatus.PENDING);
        req.setAdjustedAmount(BigDecimal.ZERO);
        SupplierAdvance saved = repo.save(req);

        BigDecimal amount = saved.getAmount();
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                postAdvanceJournal(tenantId, saved, amount);
            } catch (Exception e) {
                log.warn("GL entry skipped for supplier advance {}: {}", saved.getId(), e.getMessage());
            }
        }
        return saved;
    }

    private void postAdvanceJournal(UUID tenantId, SupplierAdvance advance, BigDecimal amount) {
        List<Account> advAccts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ADVANCE_TO_VENDOR");
        List<Account> bankAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "BANK");
        if (advAccts.isEmpty() || bankAccts.isEmpty()) return;

        BigDecimal net = amount.setScale(2, RoundingMode.HALF_UP);
        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setEntryDate(advance.getAdvanceDate() != null ? advance.getAdvanceDate() : LocalDate.now());
        je.setDescription("Supplier advance paid"
                + (advance.getVendor() != null ? " – " + advance.getVendor().getName() : ""));
        je.setStatus(JEStatus.DRAFT);
        je.setReferenceType("SUPPLIER_ADVANCE");
        je.setReferenceId(advance.getId());

        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccountId(advAccts.get(0).getId());
        dr.setDebitAmount(net);
        dr.setCreditAmount(BigDecimal.ZERO);

        JournalEntryLine cr = new JournalEntryLine();
        cr.setAccountId(bankAccts.get(0).getId());
        cr.setDebitAmount(BigDecimal.ZERO);
        cr.setCreditAmount(net);

        je.setLines(new java.util.ArrayList<>(java.util.List.of(dr, cr)));
        journalEntryService.create(je);
        log.info("GL entry posted for supplier advance {}: DR ADVANCE_TO_VENDOR / CR BANK {}", advance.getId(), net);
    }

    @Transactional
    public SupplierAdvance update(UUID id, SupplierAdvance req) {
        SupplierAdvance e = findOrThrow(id);
        if (e.getStatus() != AdvanceStatus.PENDING) {
            throw AppException.businessRule("Can only update PENDING advances");
        }
        e.setVendor(req.getVendor());
        e.setAdvanceDate(req.getAdvanceDate());
        e.setAmount(req.getAmount());
        e.setAdvanceType(req.getAdvanceType());
        e.setBankReference(req.getBankReference());
        e.setDescription(req.getDescription());
        e.setNotes(req.getNotes());
        return repo.save(e);
    }

    @Transactional
    public SupplierAdvance cancel(UUID id) {
        SupplierAdvance e = findOrThrow(id);
        if (e.getStatus() == AdvanceStatus.FULLY_ADJUSTED || e.getStatus() == AdvanceStatus.CANCELLED) {
            throw AppException.businessRule("Cannot cancel advance with status: " + e.getStatus());
        }
        e.setStatus(AdvanceStatus.CANCELLED);
        return repo.save(e);
    }

    @Transactional
    public SupplierAdvance adjust(UUID advanceId, AdvanceAdjustment adjustment) {
        SupplierAdvance advance = findOrThrow(advanceId);
        if (advance.getStatus() == AdvanceStatus.CANCELLED || advance.getStatus() == AdvanceStatus.FULLY_ADJUSTED) {
            throw AppException.businessRule("Cannot adjust advance with status: " + advance.getStatus());
        }
        BigDecimal newAdjusted = advance.getAdjustedAmount().add(adjustment.getAdjustedAmount());
        if (newAdjusted.compareTo(advance.getAmount()) > 0) {
            throw AppException.businessRule("Adjustment amount exceeds available balance");
        }
        adjustment.setAdvance(advance);
        adjustment.setTenantId(tenantContext.current());
        adjustmentRepo.save(adjustment);

        advance.setAdjustedAmount(newAdjusted);
        if (newAdjusted.compareTo(advance.getAmount()) == 0) {
            advance.setStatus(AdvanceStatus.FULLY_ADJUSTED);
        } else {
            advance.setStatus(AdvanceStatus.PARTIALLY_ADJUSTED);
        }
        return repo.save(advance);
    }

    public List<AdvanceAdjustment> getAdjustments(UUID advanceId) {
        findOrThrow(advanceId);
        return adjustmentRepo.findByAdvanceId(advanceId);
    }

    private SupplierAdvance findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Supplier advance not found: " + id));
    }
}
