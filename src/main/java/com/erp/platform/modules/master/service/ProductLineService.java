package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateProductLineRequest;
import com.erp.platform.modules.master.dto.ProductLineDto;
import com.erp.platform.modules.master.entity.Brand;
import com.erp.platform.modules.master.entity.ProductLine;
import com.erp.platform.modules.master.repository.BrandRepository;
import com.erp.platform.modules.master.repository.ProductLineRepository;
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
public class ProductLineService {

    private final ProductLineRepository productLineRepository;
    private final BrandRepository brandRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProductLineDto> list(UUID brandId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = (brandId != null)
                ? productLineRepository.findByTenantIdAndBrandIdAndDeletedAtIsNull(tenantId, brandId, pageable)
                : productLineRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public ProductLineDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductLineDto create(CreateProductLineRequest request) {
        UUID tenantId = tenantContext.current();

        ProductLine pl = new ProductLine();
        pl.setTenantId(tenantId);
        pl.setCode(request.getCode().toUpperCase());
        pl.setName(request.getName());
        pl.setDescription(request.getDescription());
        pl.setCategory(request.getCategory());
        pl.setTargetMarket(request.getTargetMarket());
        pl.setActive(request.isActive());
        pl.setNotes(request.getNotes());

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getBrandId())
                    .orElseThrow(() -> AppException.notFound("Brand not found: " + request.getBrandId()));
            pl.setBrandId(brand.getId());
            pl.setBrandName(brand.getName());
        }

        pl = productLineRepository.save(pl);
        log.info("ProductLine created: id={}, code={}", pl.getId(), pl.getCode());
        return toDto(pl);
    }

    @Transactional
    public ProductLineDto update(UUID id, CreateProductLineRequest request) {
        UUID tenantId = tenantContext.current();
        ProductLine pl = findOrThrow(id);

        pl.setCode(request.getCode().toUpperCase());
        pl.setName(request.getName());
        pl.setDescription(request.getDescription());
        pl.setCategory(request.getCategory());
        pl.setTargetMarket(request.getTargetMarket());
        pl.setActive(request.isActive());
        pl.setNotes(request.getNotes());

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getBrandId())
                    .orElseThrow(() -> AppException.notFound("Brand not found: " + request.getBrandId()));
            pl.setBrandId(brand.getId());
            pl.setBrandName(brand.getName());
        } else {
            pl.setBrandId(null);
            pl.setBrandName(null);
        }

        return toDto(productLineRepository.save(pl));
    }

    @Transactional
    public void delete(UUID id) {
        ProductLine pl = findOrThrow(id);
        pl.setDeletedAt(LocalDateTime.now());
        productLineRepository.save(pl);
        log.info("ProductLine soft-deleted: id={}", id);
    }

    private ProductLine findOrThrow(UUID id) {
        return productLineRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProductLine not found: " + id));
    }

    private ProductLineDto toDto(ProductLine pl) {
        ProductLineDto dto = new ProductLineDto();
        dto.setId(pl.getId());
        dto.setTenantId(pl.getTenantId());
        dto.setCode(pl.getCode());
        dto.setName(pl.getName());
        dto.setDescription(pl.getDescription());
        dto.setBrandId(pl.getBrandId());
        dto.setBrandName(pl.getBrandName());
        dto.setCategory(pl.getCategory());
        dto.setTargetMarket(pl.getTargetMarket());
        dto.setActive(pl.isActive());
        dto.setNotes(pl.getNotes());
        dto.setCreatedAt(pl.getCreatedAt());
        return dto;
    }
}
