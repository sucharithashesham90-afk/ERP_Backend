package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ManufacturingAppFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManufacturingAppFeatureRepository extends JpaRepository<ManufacturingAppFeature, UUID> {
    List<ManufacturingAppFeature> findByTenantId(UUID tenantId);
    Optional<ManufacturingAppFeature> findByTenantIdAndFeatureKey(UUID tenantId, String featureKey);
}
