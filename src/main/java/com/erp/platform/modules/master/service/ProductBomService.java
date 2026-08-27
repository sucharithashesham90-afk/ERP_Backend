package com.erp.platform.modules.master.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateProductBomLineRequest;
import com.erp.platform.modules.master.dto.ProductBomLineDto;
import com.erp.platform.modules.master.entity.ProductBomLine;
import com.erp.platform.modules.master.repository.ProductBomLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductBomService {

    private final ProductBomLineRepository repository;
    private final TenantContext tenantContext;

    public List<ProductBomLineDto> listByProduct(UUID productId) {
        UUID tenantId = tenantContext.current();
        return repository
                .findByTenantIdAndProductIdAndDeletedAtIsNullOrderBySortOrderAscCreatedAtAsc(tenantId, productId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ProductBomLineDto> listByBrand(UUID brandId) {
        UUID tenantId = tenantContext.current();
        return repository
                .findByTenantIdAndBrandIdAndDeletedAtIsNullOrderBySortOrderAscCreatedAtAsc(tenantId, brandId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ProductBomLineDto create(CreateProductBomLineRequest req) {
        UUID tenantId = tenantContext.current();
        ProductBomLine line = new ProductBomLine();
        line.setTenantId(tenantId);
        map(line, req);
        ProductBomLine saved = repository.save(line);
        log.info("ProductBomLine created: id={}, product={}", saved.getId(), saved.getProductId());
        return toDto(saved);
    }

    @Transactional
    public ProductBomLineDto update(UUID id, CreateProductBomLineRequest req) {
        UUID tenantId = tenantContext.current();
        ProductBomLine line = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Product BOM line not found: " + id));
        map(line, req);
        return toDto(repository.save(line));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ProductBomLine line = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Product BOM line not found: " + id));
        line.setDeletedAt(LocalDateTime.now());
        repository.save(line);
    }

    private void map(ProductBomLine line, CreateProductBomLineRequest req) {
        // A BOM is defined against a Brand; product_id mirrors brand_id (kept non-null) for legacy queries.
        UUID parentId = req.getBrandId() != null ? req.getBrandId() : req.getProductId();
        line.setProductId(parentId);
        line.setProductName(req.getProductName());
        line.setProductCode(req.getProductCode());
        line.setBrandId(req.getBrandId());
        line.setBrandName(req.getBrandName());
        line.setMaterialGroupId(req.getMaterialGroupId());
        line.setMaterialGroupName(req.getMaterialGroupName());
        line.setMaterialItemId(req.getMaterialItemId());
        line.setMaterialItemName(req.getMaterialItemName());
        line.setBomType(req.getBomType() != null ? req.getBomType() : "PACKING");
        line.setQtyPerPack(req.getQtyPerPack() != null ? req.getQtyPerPack() : BigDecimal.ONE);
        line.setUnit(req.getUnit());
        line.setMrp(req.getMrp() != null ? req.getMrp() : BigDecimal.ZERO);
        line.setNotes(req.getNotes());
        line.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
    }

    private ProductBomLineDto toDto(ProductBomLine e) {
        ProductBomLineDto dto = new ProductBomLineDto();
        dto.setId(e.getId());
        dto.setProductId(e.getProductId());
        dto.setProductName(e.getProductName());
        dto.setProductCode(e.getProductCode());
        dto.setBrandId(e.getBrandId());
        dto.setBrandName(e.getBrandName());
        dto.setMaterialGroupId(e.getMaterialGroupId());
        dto.setMaterialGroupName(e.getMaterialGroupName());
        dto.setMaterialItemId(e.getMaterialItemId());
        dto.setMaterialItemName(e.getMaterialItemName());
        dto.setBomType(e.getBomType());
        dto.setQtyPerPack(e.getQtyPerPack());
        dto.setUnit(e.getUnit());
        dto.setMrp(e.getMrp());
        dto.setNotes(e.getNotes());
        dto.setSortOrder(e.getSortOrder());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
