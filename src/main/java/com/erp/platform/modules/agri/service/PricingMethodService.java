package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePricingMethodRequest;
import com.erp.platform.modules.agri.dto.PricingMethodDto;
import com.erp.platform.modules.agri.entity.PricingMethod;
import com.erp.platform.modules.agri.repository.PricingMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingMethodService {

    private final PricingMethodRepository pricingMethodRepository;
    private final TenantContext tenantContext;

    public PageResponse<PricingMethodDto> list(Pageable pageable) {
        return PageResponse.of(pricingMethodRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public PricingMethodDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        PricingMethod entity = pricingMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PricingMethod not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public PricingMethodDto create(CreatePricingMethodRequest request) {
        PricingMethod entity = new PricingMethod();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        return toDto(pricingMethodRepository.save(entity));
    }

    @Transactional
    public PricingMethodDto update(UUID id, CreatePricingMethodRequest request) {
        UUID tenantId = tenantContext.current();
        PricingMethod entity = pricingMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PricingMethod not found: " + id));
        applyRequest(entity, request);
        return toDto(pricingMethodRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        PricingMethod entity = pricingMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PricingMethod not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        pricingMethodRepository.save(entity);
    }

    private static UUID parseUuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s.trim()); } catch (IllegalArgumentException ex) { return null; }
    }

    private void applyRequest(PricingMethod e, CreatePricingMethodRequest r) {
        e.setMethodName(r.getMethodName());
        e.setProcurementSeedState(r.getProcurementSeedState());
        e.setPricingBasedOnSeedState(r.getPricingBasedOnSeedState());
        e.setLiabilityForPayment(r.getLiabilityForPayment());
        e.setProcessingStepId(parseUuidOrNull(r.getProcessingStepId()));
        e.setProcessingStepName(r.getProcessingStepName());
        e.setLiabilityPaymentTo(r.getLiabilityPaymentTo());
        e.setQualityTesting(r.getQualityTesting());
        e.setActive(r.isActive());
    }

    private PricingMethodDto toDto(PricingMethod e) {
        PricingMethodDto dto = new PricingMethodDto();
        dto.setId(e.getId());
        dto.setMethodName(e.getMethodName());
        dto.setProcurementSeedState(e.getProcurementSeedState());
        dto.setPricingBasedOnSeedState(e.getPricingBasedOnSeedState());
        dto.setLiabilityForPayment(e.getLiabilityForPayment());
        dto.setProcessingStepId(e.getProcessingStepId());
        dto.setProcessingStepName(e.getProcessingStepName());
        dto.setLiabilityPaymentTo(e.getLiabilityPaymentTo());
        dto.setQualityTesting(e.getQualityTesting());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
