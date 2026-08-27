package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateProductRequest;
import com.erp.platform.modules.master.dto.ProductDto;
import com.erp.platform.modules.master.dto.UpdateProductRequest;
import com.erp.platform.modules.master.entity.MaterialGroup;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.entity.ProductCategory;
import com.erp.platform.modules.master.entity.Tax;
import com.erp.platform.modules.master.mapper.ProductMapper;
import com.erp.platform.modules.master.repository.MaterialGroupRepository;
import com.erp.platform.modules.master.repository.ProductCategoryRepository;
import com.erp.platform.modules.master.repository.ProductRepository;
import com.erp.platform.modules.master.repository.TaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryRepository categoryRepository;
    private final MaterialGroupRepository materialGroupRepository;
    private final TaxRepository taxRepository;
    private final TenantContext tenantContext;

    private static final List<Product.ProductType> MATERIAL_TYPES =
            List.of(Product.ProductType.RAW_MATERIAL, Product.ProductType.CONSUMABLE, Product.ProductType.ASSET);
    private static final List<Product.ProductType> PRODUCT_TYPES =
            List.of(Product.ProductType.GOODS, Product.ProductType.SERVICE);

    public PageResponse<ProductDto> list(String search, String category, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<Product.ProductType> types = resolveCategory(category);
        if (types != null) {
            var page = StringUtils.hasText(search)
                    ? productRepository.searchByTenantIdAndProductTypeIn(tenantId, search, types, pageable)
                    : productRepository.findByTenantIdAndProductTypeIn(tenantId, types, pageable);
            return PageResponse.of(page.map(productMapper::toDto));
        }
        var page = StringUtils.hasText(search)
                ? productRepository.searchByTenantId(tenantId, search, pageable)
                : productRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(productMapper::toDto));
    }

    private List<Product.ProductType> resolveCategory(String category) {
        if ("MATERIAL".equalsIgnoreCase(category)) return MATERIAL_TYPES;
        if ("PRODUCT".equalsIgnoreCase(category)) return PRODUCT_TYPES;
        return null;
    }

    public ProductDto getById(UUID id) {
        return productMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(request.getCode())
                && productRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())) {
            throw AppException.conflict("Product with code already exists: " + request.getCode());
        }
        Product product = productMapper.toEntity(request);
        product.setTenantId(tenantId);
        if (!StringUtils.hasText(product.getCode())) {
            product.setCode("PROD-" + System.currentTimeMillis());
        }
        // Set category relationship
        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCategoryId())
                    .orElseThrow(() -> AppException.notFound("Product category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        // Set material group (denormalised name for fast reads)
        resolveMaterialGroup(product, tenantId, request.getMaterialGroupId());
        // Set tax rate relationship
        if (request.getTaxRateId() != null) {
            Tax tax = taxRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getTaxRateId())
                    .orElseThrow(() -> AppException.notFound("Tax rate not found: " + request.getTaxRateId()));
            product.setTaxRate(tax);
        }
        product = productRepository.save(product);
        log.info("Product created: id={}, name={}", product.getId(), product.getName());
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto update(UUID id, UpdateProductRequest request) {
        UUID tenantId = tenantContext.current();
        Product product = findOrThrow(id);
        productMapper.updateEntity(request, product);
        // Update category relationship
        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCategoryId())
                    .orElseThrow(() -> AppException.notFound("Product category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        // Update material group
        resolveMaterialGroup(product, tenantId, request.getMaterialGroupId());
        // Update tax rate relationship
        if (request.getTaxRateId() != null) {
            Tax tax = taxRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getTaxRateId())
                    .orElseThrow(() -> AppException.notFound("Tax rate not found: " + request.getTaxRateId()));
            product.setTaxRate(tax);
        }
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findOrThrow(id);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    private void resolveMaterialGroup(Product product, UUID tenantId, UUID materialGroupId) {
        if (materialGroupId != null) {
            MaterialGroup mg = materialGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, materialGroupId)
                    .orElseThrow(() -> AppException.notFound("Material group not found: " + materialGroupId));
            product.setMaterialGroupId(mg.getId());
            product.setMaterialGroupName(mg.getName());
        } else {
            product.setMaterialGroupId(null);
            product.setMaterialGroupName(null);
        }
    }

    private Product findOrThrow(UUID id) {
        return productRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Product not found: " + id));
    }
}
