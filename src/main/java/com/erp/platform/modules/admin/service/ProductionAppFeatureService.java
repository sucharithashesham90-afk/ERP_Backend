package com.erp.platform.modules.admin.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.admin.dto.CreateProductionAppFeatureRequest;
import com.erp.platform.modules.admin.dto.ProductionAppFeatureDto;
import com.erp.platform.modules.admin.entity.ProductionAppFeature;
import com.erp.platform.modules.admin.repository.ProductionAppFeatureRepository;
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
public class ProductionAppFeatureService {

    private final ProductionAppFeatureRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionAppFeatureDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProductionAppFeatureDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionAppFeatureDto create(CreateProductionAppFeatureRequest request) {
        UUID tenantId = tenantContext.current();
        ProductionAppFeature entity = new ProductionAppFeature();
        entity.setTenantId(tenantId);
        entity.setFeatureKey(request.featureKey());
        entity.setFeatureName(request.featureName());
        entity.setDescription(request.description());
        entity.setEnabled(request.enabled());
        entity.setModuleContext(request.moduleContext());
        entity = repository.save(entity);
        log.info("ProductionAppFeature created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProductionAppFeatureDto update(UUID id, CreateProductionAppFeatureRequest request) {
        ProductionAppFeature entity = findOrThrow(id);
        entity.setFeatureKey(request.featureKey());
        entity.setFeatureName(request.featureName());
        entity.setDescription(request.description());
        entity.setEnabled(request.enabled());
        entity.setModuleContext(request.moduleContext());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProductionAppFeature entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProductionAppFeature findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProductionAppFeature not found: " + id));
    }

    private ProductionAppFeatureDto toDto(ProductionAppFeature e) {
        return new ProductionAppFeatureDto(
                e.getId(),
                e.getFeatureKey(),
                e.getFeatureName(),
                e.getDescription(),
                e.isEnabled(),
                e.getModuleContext()
        );
    }
}
