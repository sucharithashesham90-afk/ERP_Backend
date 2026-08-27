package com.erp.platform.modules.supplier.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.supplier.dto.CreateSupplierPerformanceRequest;
import com.erp.platform.modules.supplier.dto.SupplierPerformanceDto;
import com.erp.platform.modules.supplier.entity.SupplierPerformance;
import com.erp.platform.modules.supplier.entity.SupplierPerformance.PerfStatus;
import com.erp.platform.modules.supplier.repository.SupplierPerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierPerformanceService {

    private final SupplierPerformanceRepository perfRepo;
    private final TenantContext tenantContext;

    public PageResponse<SupplierPerformanceDto> list(UUID vendorId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = vendorId != null
                ? perfRepo.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable)
                : perfRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public SupplierPerformanceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SupplierPerformanceDto create(CreateSupplierPerformanceRequest request) {
        UUID tenantId = tenantContext.current();
        SupplierPerformance perf = new SupplierPerformance();
        perf.setTenantId(tenantId);
        mapRequest(perf, request);
        computeOverallAndStatus(perf);
        perf = perfRepo.save(perf);
        log.info("SupplierPerformance created for vendor: {}", perf.getVendorId());
        return toDto(perf);
    }

    @Transactional
    public SupplierPerformanceDto update(UUID id, CreateSupplierPerformanceRequest request) {
        SupplierPerformance perf = findOrThrow(id);
        mapRequest(perf, request);
        computeOverallAndStatus(perf);
        return toDto(perfRepo.save(perf));
    }

    private void mapRequest(SupplierPerformance p, CreateSupplierPerformanceRequest r) {
        if (r.getVendorId() != null) p.setVendorId(r.getVendorId());
        if (r.getVendorName() != null) p.setVendorName(r.getVendorName());
        p.setEvaluationPeriod(r.getEvaluationPeriod());
        p.setEvaluationDate(r.getEvaluationDate() != null ? r.getEvaluationDate() : LocalDate.now());
        p.setOnTimeDeliveryScore(r.getOnTimeDeliveryScore());
        p.setQualityScore(r.getQualityScore());
        p.setResponseScore(r.getResponseScore());
        p.setPricingScore(r.getPricingScore());
        p.setTotalOrders(r.getTotalOrders());
        p.setOnTimeOrders(r.getOnTimeOrders());
        p.setLateOrders(r.getLateOrders());
        p.setRejectedLots(r.getRejectedLots());
        p.setAcceptedLots(r.getAcceptedLots());
        p.setRemarks(r.getRemarks());
        p.setEvaluatedBy(r.getEvaluatedBy());
    }

    private void computeOverallAndStatus(SupplierPerformance p) {
        BigDecimal s1 = nvl(p.getOnTimeDeliveryScore());
        BigDecimal s2 = nvl(p.getQualityScore());
        BigDecimal s3 = nvl(p.getResponseScore());
        BigDecimal s4 = nvl(p.getPricingScore());
        BigDecimal overall = s1.add(s2).add(s3).add(s4).divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        p.setOverallScore(overall);
        int score = overall.intValue();
        if (score >= 80) p.setStatus(PerfStatus.PREFERRED);
        else if (score >= 60) p.setStatus(PerfStatus.APPROVED);
        else if (score >= 40) p.setStatus(PerfStatus.CONDITIONAL);
        else p.setStatus(PerfStatus.BLACKLISTED);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private SupplierPerformanceDto toDto(SupplierPerformance p) {
        SupplierPerformanceDto dto = new SupplierPerformanceDto();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setVendorId(p.getVendorId());
        dto.setVendorName(p.getVendorName());
        dto.setEvaluationPeriod(p.getEvaluationPeriod());
        dto.setEvaluationDate(p.getEvaluationDate());
        dto.setOnTimeDeliveryScore(p.getOnTimeDeliveryScore());
        dto.setQualityScore(p.getQualityScore());
        dto.setResponseScore(p.getResponseScore());
        dto.setPricingScore(p.getPricingScore());
        dto.setOverallScore(p.getOverallScore());
        dto.setTotalOrders(p.getTotalOrders());
        dto.setOnTimeOrders(p.getOnTimeOrders());
        dto.setLateOrders(p.getLateOrders());
        dto.setRejectedLots(p.getRejectedLots());
        dto.setAcceptedLots(p.getAcceptedLots());
        dto.setStatus(p.getStatus());
        dto.setRemarks(p.getRemarks());
        dto.setEvaluatedBy(p.getEvaluatedBy());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    private SupplierPerformance findOrThrow(UUID id) {
        return perfRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Supplier performance record not found: " + id));
    }
}
