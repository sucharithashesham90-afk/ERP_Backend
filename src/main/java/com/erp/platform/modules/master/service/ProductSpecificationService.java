package com.erp.platform.modules.master.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.entity.ProductSpecification;
import com.erp.platform.modules.master.repository.ProductRepository;
import com.erp.platform.modules.master.repository.ProductSpecificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductSpecificationService {

    private final ProductSpecificationRepository repo;
    private final ProductRepository productRepo;
    private final TenantContext tenantContext;

    public List<ProductSpecification> getByProduct(UUID productId) {
        findProductOrThrow(productId);
        return repo.findByProductIdAndDeletedAtIsNullOrderByDisplayOrder(productId);
    }

    @Transactional
    public ProductSpecification create(UUID productId, ProductSpecification req) {
        Product product = findProductOrThrow(productId);
        req.setProduct(product);
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public ProductSpecification update(UUID id, ProductSpecification req) {
        ProductSpecification e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Product specification not found: " + id));
        e.setSpecType(req.getSpecType());
        e.setAttributeName(req.getAttributeName());
        e.setAttributeValue(req.getAttributeValue());
        e.setUnit(req.getUnit());
        e.setMinValue(req.getMinValue());
        e.setMaxValue(req.getMaxValue());
        e.setMandatory(req.isMandatory());
        e.setDisplayOrder(req.getDisplayOrder());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        ProductSpecification e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Product specification not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    @Transactional
    public List<ProductSpecification> bulkSave(UUID productId, List<ProductSpecification> specs) {
        Product product = findProductOrThrow(productId);
        UUID tenantId = tenantContext.current();

        // Soft-delete existing specs for this product
        List<ProductSpecification> existing = repo.findByProductIdAndDeletedAtIsNullOrderByDisplayOrder(productId);
        LocalDateTime now = LocalDateTime.now();
        existing.forEach(e -> e.setDeletedAt(now));
        repo.saveAll(existing);

        // Save new specs
        List<ProductSpecification> saved = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            ProductSpecification spec = specs.get(i);
            spec.setProduct(product);
            spec.setTenantId(tenantId);
            spec.setDisplayOrder(i);
            saved.add(repo.save(spec));
        }
        return saved;
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), productId)
                .orElseThrow(() -> AppException.notFound("Product not found: " + productId));
    }
}
