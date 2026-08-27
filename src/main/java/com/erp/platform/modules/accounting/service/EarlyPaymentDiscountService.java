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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EarlyPaymentDiscountService {

    private final EarlyPaymentDiscountRepository repo;
    private final PaymentTermRepository paymentTermRepo;
    private final TenantContext tenantContext;

    public PageResponse<EarlyPaymentDiscount> list(boolean applied, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repo.findByTenantIdAndApplied(tenantId, applied, pageable));
    }

    public List<EarlyPaymentDiscount> getEligible() {
        UUID tenantId = tenantContext.current();
        return repo.findByTenantIdAndAppliedAndDiscountEligibleUntilGreaterThanEqual(tenantId, false, LocalDate.now());
    }

    @Transactional
    public EarlyPaymentDiscount create(Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        EarlyPaymentDiscount epd = new EarlyPaymentDiscount();
        epd.setTenantId(tenantId);

        if (body.get("invoiceId") != null && !body.get("invoiceId").toString().isBlank()) {
            epd.setInvoiceId(UUID.fromString(body.get("invoiceId").toString()));
        }
        epd.setInvoiceNumber(body.getOrDefault("invoiceNumber", "").toString());
        if (body.get("customerId") != null && !body.get("customerId").toString().isBlank()) {
            epd.setCustomerId(UUID.fromString(body.get("customerId").toString()));
        }
        epd.setCustomerName(body.getOrDefault("customerName", "").toString());

        String invoiceDateStr = body.getOrDefault("invoiceDate", LocalDate.now().toString()).toString();
        LocalDate invoiceDate = LocalDate.parse(invoiceDateStr);
        epd.setInvoiceDate(invoiceDate);

        BigDecimal invoiceAmount = new BigDecimal(body.getOrDefault("invoiceAmount", "0").toString());
        epd.setInvoiceAmount(invoiceAmount);

        // Resolve discount percent and eligible date from payment term if provided
        BigDecimal discountPct = BigDecimal.ZERO;
        LocalDate eligibleUntil = invoiceDate.plusDays(10); // default: 10 days
        if (body.get("paymentTermId") != null && !body.get("paymentTermId").toString().isBlank()) {
            UUID ptId = UUID.fromString(body.get("paymentTermId").toString());
            epd.setPaymentTermId(ptId);
            paymentTermRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ptId).ifPresent(pt -> {
                epd.setDiscountPercent(pt.getDiscountPercent() != null ? pt.getDiscountPercent() : BigDecimal.ZERO);
                int days = pt.getDiscountDays() > 0 ? pt.getDiscountDays() : 10;
                epd.setDiscountEligibleUntil(invoiceDate.plusDays(days));
            });
        } else {
            epd.setDiscountPercent(discountPct);
            epd.setDiscountEligibleUntil(eligibleUntil);
        }

        // Compute discount amount
        BigDecimal pct = epd.getDiscountPercent() != null ? epd.getDiscountPercent() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoiceAmount.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        epd.setDiscountAmount(discountAmount);
        epd.setApplied(false);

        EarlyPaymentDiscount saved = repo.save(epd);
        log.info("Early payment discount created: {}", saved.getId());
        return saved;
    }

    @Transactional
    public EarlyPaymentDiscount apply(UUID id, String voucherReference) {
        UUID tenantId = tenantContext.current();
        EarlyPaymentDiscount epd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Early payment discount not found: " + id));
        if (epd.isApplied()) {
            throw AppException.badRequest("Discount already applied");
        }
        if (epd.getDiscountEligibleUntil() != null && LocalDate.now().isAfter(epd.getDiscountEligibleUntil())) {
            throw AppException.badRequest("Discount eligibility period has expired");
        }
        epd.setApplied(true);
        epd.setAppliedDate(LocalDate.now());
        epd.setVoucherReference(voucherReference);
        log.info("Early payment discount {} applied, voucher={}", id, voucherReference);
        return repo.save(epd);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        EarlyPaymentDiscount epd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Early payment discount not found: " + id));
        if (epd.isApplied()) {
            throw AppException.badRequest("Cannot delete an already applied discount");
        }
        epd.setDeletedAt(LocalDateTime.now());
        repo.save(epd);
    }
}
