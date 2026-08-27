package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.BrandDto;
import com.erp.platform.modules.master.dto.CreateBrandRequest;
import com.erp.platform.modules.master.entity.Brand;
import com.erp.platform.modules.master.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final TenantContext tenantContext;

    public PageResponse<BrandDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(brandRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public BrandDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public BrandDto create(CreateBrandRequest request) {
        UUID tenantId = tenantContext.current();
        brandRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())
                .ifPresent(e -> { throw AppException.conflict("Brand with code already exists: " + request.getCode()); });

        Brand brand = new Brand();
        brand.setTenantId(tenantId);
        brand.setCode(request.getCode().toUpperCase());
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setCountry(request.getCountry());
        applyBrandFields(brand, request);
        checkVarietyUnique(tenantId, brand.getVarietyId(), null);
        brand.setActive(request.isActive());
        brand.setNotes(request.getNotes());

        brand = brandRepository.save(brand);
        log.info("Brand created: id={}, code={}", brand.getId(), brand.getCode());
        return toDto(brand);
    }

    @Transactional
    public BrandDto update(UUID id, CreateBrandRequest request) {
        UUID tenantId = tenantContext.current();
        Brand brand = findOrThrow(id);

        if (!brand.getCode().equalsIgnoreCase(request.getCode())) {
            brandRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())
                    .ifPresent(e -> { throw AppException.conflict("Brand with code already exists: " + request.getCode()); });
            brand.setCode(request.getCode().toUpperCase());
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setCountry(request.getCountry());
        applyBrandFields(brand, request);
        checkVarietyUnique(tenantId, brand.getVarietyId(), brand.getId());
        brand.setActive(request.isActive());
        brand.setNotes(request.getNotes());

        return toDto(brandRepository.save(brand));
    }

    private void applyBrandFields(Brand brand, CreateBrandRequest request) {
        brand.setCropGroupId(blankToNull(request.getCropGroupId()));
        brand.setCropGroupName(request.getCropGroupName());
        brand.setCropId(blankToNull(request.getCropId()));
        brand.setCropName(request.getCropName());
        brand.setVarietyId(blankToNull(request.getVarietyId()));
        brand.setVarietyName(request.getVarietyName());
        brand.setSalesScope(request.getSalesScope() != null ? request.getSalesScope() : "ALL");
        brand.setSalesAreas("SELECTED".equalsIgnoreCase(request.getSalesScope()) ? request.getSalesAreas() : null);
        brand.setUseSticker(request.isUseSticker());
        brand.setStickerMaterial(request.isUseSticker() ? request.getStickerMaterial() : null);
        brand.setImageData(request.getImageData());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Enforce the 1:1 brand↔variety rule: a variety can back at most one brand. */
    private void checkVarietyUnique(UUID tenantId, String varietyId, UUID selfId) {
        if (varietyId == null || varietyId.isBlank()) return;
        brandRepository.findByTenantIdAndVarietyIdAndDeletedAtIsNull(tenantId, varietyId).stream()
                .filter(b -> selfId == null || !b.getId().equals(selfId))
                .findFirst()
                .ifPresent(b -> { throw AppException.conflict("This variety is already assigned to brand: " + b.getName()); });
    }

    @Transactional
    public void delete(UUID id) {
        Brand brand = findOrThrow(id);
        brand.setDeletedAt(LocalDateTime.now());
        brandRepository.save(brand);
        log.info("Brand soft-deleted: id={}", id);
    }

    private Brand findOrThrow(UUID id) {
        return brandRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Brand not found: " + id));
    }

    private BrandDto toDto(Brand b) {
        BrandDto dto = new BrandDto();
        dto.setId(b.getId());
        dto.setTenantId(b.getTenantId());
        dto.setCode(b.getCode());
        dto.setName(b.getName());
        dto.setCropGroupId(b.getCropGroupId());
        dto.setCropGroupName(b.getCropGroupName());
        dto.setCropId(b.getCropId());
        dto.setCropName(b.getCropName());
        dto.setVarietyId(b.getVarietyId());
        dto.setVarietyName(b.getVarietyName());
        dto.setDescription(b.getDescription());
        dto.setLogoUrl(b.getLogoUrl());
        dto.setCountry(b.getCountry());
        dto.setSalesScope(b.getSalesScope());
        dto.setSalesAreas(b.getSalesAreas());
        dto.setUseSticker(Boolean.TRUE.equals(b.getUseSticker()));
        dto.setStickerMaterial(b.getStickerMaterial());
        dto.setImageData(b.getImageData());
        dto.setActive(b.isActive());
        dto.setNotes(b.getNotes());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
