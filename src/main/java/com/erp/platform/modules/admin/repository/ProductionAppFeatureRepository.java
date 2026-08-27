package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.ProductionAppFeature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductionAppFeatureRepository extends JpaRepository<ProductionAppFeature, UUID> {

    Page<ProductionAppFeature> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProductionAppFeature> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
