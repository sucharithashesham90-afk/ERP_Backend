package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePackagingDesignRequest;
import com.erp.platform.modules.agri.dto.PackagingDesignDto;
import com.erp.platform.modules.agri.entity.PackagingDesign;
import com.erp.platform.modules.agri.repository.PackagingDesignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PackagingDesignService {

    private final PackagingDesignRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<PackagingDesignDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PackagingDesignDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PackagingDesignDto create(CreatePackagingDesignRequest request) {
        UUID tenantId = tenantContext.current();
        PackagingDesign entity = new PackagingDesign();
        entity.setTenantId(tenantId);
        entity.setProductName(request.productName());
        entity.setVarietyLabel(request.varietyLabel());
        entity.setBrandCode(request.brandCode());
        entity.setBrandName(request.brandName());
        entity.setPackingMaterial(request.packingMaterial());
        entity.setPackingQty(request.packingQty());
        entity.setNetWeightKg(request.netWeightKg());
        entity.setGrossWeightKg(request.grossWeightKg());
        entity.setDimensionLength(request.dimensionLength());
        entity.setDimensionWidth(request.dimensionWidth());
        entity.setDimensionHeight(request.dimensionHeight());
        entity.setPackSize(request.packSize());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("PackagingDesign created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PackagingDesignDto update(UUID id, CreatePackagingDesignRequest request) {
        PackagingDesign entity = findOrThrow(id);
        entity.setProductName(request.productName());
        entity.setVarietyLabel(request.varietyLabel());
        entity.setBrandCode(request.brandCode());
        entity.setBrandName(request.brandName());
        entity.setPackingMaterial(request.packingMaterial());
        entity.setPackingQty(request.packingQty());
        entity.setNetWeightKg(request.netWeightKg());
        entity.setGrossWeightKg(request.grossWeightKg());
        entity.setDimensionLength(request.dimensionLength());
        entity.setDimensionWidth(request.dimensionWidth());
        entity.setDimensionHeight(request.dimensionHeight());
        entity.setPackSize(request.packSize());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PackagingDesign entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PackagingDesign findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PackagingDesign not found: " + id));
    }

    private PackagingDesignDto toDto(PackagingDesign e) {
        return new PackagingDesignDto(
                e.getId(),
                e.getProductName(),
                e.getVarietyLabel(),
                e.getBrandCode(),
                e.getBrandName(),
                e.getPackingMaterial(),
                e.getPackingQty(),
                e.getNetWeightKg(),
                e.getGrossWeightKg(),
                e.getDimensionLength(),
                e.getDimensionWidth(),
                e.getDimensionHeight(),
                e.getPackSize(),
                e.isActive()
        );
    }
}
