package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.EarlyPaymentDiscount;
import com.erp.platform.modules.accounting.entity.PaymentTerm;
import com.erp.platform.modules.accounting.repository.EarlyPaymentDiscountRepository;
import com.erp.platform.modules.accounting.repository.PaymentTermRepository;
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
public class PaymentTermService {

    private final PaymentTermRepository paymentTermRepo;
    private final EarlyPaymentDiscountRepository discountRepo;
    private final TenantContext tenantContext;

    // ---- Payment Terms ----

    public PageResponse<PaymentTerm> list(Pageable pageable) {
        return PageResponse.of(paymentTermRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public List<PaymentTerm> getAllActive() {
        return paymentTermRepo.findByTenantIdAndActiveAndDeletedAtIsNull(tenantContext.current(), true);
    }

    @Transactional
    public PaymentTerm create(PaymentTerm req) {
        req.setTenantId(tenantContext.current());
        return paymentTermRepo.save(req);
    }

    @Transactional
    public PaymentTerm update(UUID id, PaymentTerm req) {
        PaymentTerm e = paymentTermRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Payment term not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setDueDays(req.getDueDays());
        e.setDiscountDays(req.getDiscountDays());
        e.setDiscountPercent(req.getDiscountPercent());
        e.setHasEarlyPaymentDiscount(req.isHasEarlyPaymentDiscount());
        e.setPenaltyPercent(req.getPenaltyPercent());
        e.setPenaltyAfterDays(req.getPenaltyAfterDays());
        e.setActive(req.isActive());
        return paymentTermRepo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        PaymentTerm e = paymentTermRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Payment term not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        paymentTermRepo.save(e);
    }

    // ---- Early Payment Discounts ----

    @Transactional
    public EarlyPaymentDiscount createEarlyPaymentDiscount(EarlyPaymentDiscount req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);

        // Look up payment term if provided
        if (req.getPaymentTermId() != null) {
            paymentTermRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPaymentTermId())
                    .ifPresent(term -> {
                        if (req.getDiscountPercent() == null || req.getDiscountPercent().compareTo(BigDecimal.ZERO) == 0) {
                            req.setDiscountPercent(term.getDiscountPercent());
                        }
                        if (req.getInvoiceDate() != null && req.getDiscountEligibleUntil() == null) {
                            req.setDiscountEligibleUntil(req.getInvoiceDate().plusDays(term.getDiscountDays()));
                        }
                    });
        }

        // Calculate discount amount
        if (req.getInvoiceAmount() != null && req.getDiscountPercent() != null) {
            BigDecimal discountAmount = req.getInvoiceAmount()
                    .multiply(req.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            req.setDiscountAmount(discountAmount);
        }

        // Calculate eligibleUntil if not set
        if (req.getDiscountEligibleUntil() == null && req.getInvoiceDate() != null) {
            req.setDiscountEligibleUntil(req.getInvoiceDate());
        }

        req.setApplied(false);
        return discountRepo.save(req);
    }

    @Transactional
    public EarlyPaymentDiscount applyDiscount(UUID discountId, String voucherRef) {
        EarlyPaymentDiscount e = discountRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), discountId)
                .orElseThrow(() -> AppException.notFound("Early payment discount not found: " + discountId));
        if (e.isApplied()) {
            throw AppException.businessRule("Discount already applied");
        }
        e.setApplied(true);
        e.setAppliedDate(LocalDate.now());
        e.setVoucherReference(voucherRef);
        return discountRepo.save(e);
    }

    public PageResponse<EarlyPaymentDiscount> getPendingDiscounts(Pageable pageable) {
        return PageResponse.of(discountRepo.findByTenantIdAndApplied(tenantContext.current(), false, pageable));
    }

    public PageResponse<EarlyPaymentDiscount> getAppliedDiscounts(Pageable pageable) {
        return PageResponse.of(discountRepo.findByTenantIdAndApplied(tenantContext.current(), true, pageable));
    }

    public List<EarlyPaymentDiscount> getEligibleDiscounts() {
        return discountRepo.findByTenantIdAndAppliedAndDiscountEligibleUntilGreaterThanEqual(
                tenantContext.current(), false, LocalDate.now());
    }
}
