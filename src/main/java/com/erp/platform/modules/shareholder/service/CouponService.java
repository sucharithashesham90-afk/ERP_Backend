package com.erp.platform.modules.shareholder.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.shareholder.dto.CouponDto;
import com.erp.platform.modules.shareholder.entity.Coupon;
import com.erp.platform.modules.shareholder.repository.CouponRepository;
import com.erp.platform.modules.shareholder.repository.ShareHolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository repository;
    private final ShareHolderRepository holderRepository;
    private final TenantContext tenantContext;

    public PageResponse<CouponDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public CouponDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public List<CouponDto> listByShareHolder(UUID shareholderId) {
        return repository.findByTenantIdAndShareholderIdAndDeletedAtIsNull(tenantContext.current(), shareholderId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public CouponDto issue(CouponDto req) {
        UUID tenantId = tenantContext.current();
        if (req.getShareholderId() == null) throw AppException.badRequest("shareholderId is required");

        Coupon entity = new Coupon();
        entity.setTenantId(tenantId);
        entity.setCouponNumber(generateNumber());
        entity.setShareholderId(req.getShareholderId());

        final Coupon couponRef = entity;
        holderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getShareholderId())
                .ifPresent(h -> {
                    couponRef.setShareholderName(h.getName());
                    if (req.getSharesCount() == null) couponRef.setSharesCount(h.getSharesHeld());
                    if (req.getFaceValuePerShare() == null) couponRef.setFaceValuePerShare(h.getFaceValuePerShare());
                });

        if (req.getSharesCount() != null) entity.setSharesCount(req.getSharesCount());
        if (req.getFaceValuePerShare() != null) entity.setFaceValuePerShare(req.getFaceValuePerShare());
        entity.setDividendPercent(req.getDividendPercent());

        // Auto-compute dividend amount if percent is provided
        if (req.getDividendAmount() != null) {
            entity.setDividendAmount(req.getDividendAmount());
        } else if (entity.getSharesCount() != null && entity.getFaceValuePerShare() != null && req.getDividendPercent() != null) {
            BigDecimal faceValue = entity.getSharesCount().multiply(entity.getFaceValuePerShare());
            entity.setDividendAmount(faceValue.multiply(req.getDividendPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        entity.setIssueDate(req.getIssueDate() != null ? req.getIssueDate() : LocalDate.now());
        entity.setMaturityDate(req.getMaturityDate());
        entity.setFinancialYear(req.getFinancialYear());
        entity.setStatus("ISSUED");
        entity.setRemarks(req.getRemarks());

        entity = repository.save(entity);
        log.info("Coupon issued: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public CouponDto markPaid(UUID id, LocalDate paidDate, String paymentReference) {
        Coupon entity = findOrThrow(id);
        if ("PAID".equals(entity.getStatus())) {
            throw AppException.badRequest("Coupon is already PAID");
        }
        if ("CANCELLED".equals(entity.getStatus())) {
            throw AppException.badRequest("Cancelled coupons cannot be paid");
        }
        entity.setStatus("PAID");
        entity.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        entity.setPaymentReference(paymentReference);
        entity = repository.save(entity);
        log.info("Coupon marked PAID: {}", id);
        return toDto(entity);
    }

    @Transactional
    public CouponDto cancel(UUID id) {
        Coupon entity = findOrThrow(id);
        if ("PAID".equals(entity.getStatus())) {
            throw AppException.badRequest("Paid coupons cannot be cancelled");
        }
        entity.setStatus("CANCELLED");
        entity = repository.save(entity);
        log.info("Coupon cancelled: {}", id);
        return toDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        Coupon entity = findOrThrow(id);
        if ("PAID".equals(entity.getStatus())) {
            throw AppException.badRequest("Paid coupons cannot be deleted");
        }
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private Coupon findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Coupon not found: " + id));
    }

    private CouponDto toDto(Coupon e) {
        CouponDto dto = new CouponDto();
        dto.setId(e.getId());
        dto.setCouponNumber(e.getCouponNumber());
        dto.setShareholderId(e.getShareholderId());
        dto.setShareholderName(e.getShareholderName());
        dto.setSharesCount(e.getSharesCount());
        dto.setFaceValuePerShare(e.getFaceValuePerShare());
        dto.setDividendPercent(e.getDividendPercent());
        dto.setDividendAmount(e.getDividendAmount());
        dto.setIssueDate(e.getIssueDate());
        dto.setMaturityDate(e.getMaturityDate());
        dto.setFinancialYear(e.getFinancialYear());
        dto.setStatus(e.getStatus());
        dto.setPaidDate(e.getPaidDate());
        dto.setPaymentReference(e.getPaymentReference());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private String generateNumber() {
        return "CPN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
