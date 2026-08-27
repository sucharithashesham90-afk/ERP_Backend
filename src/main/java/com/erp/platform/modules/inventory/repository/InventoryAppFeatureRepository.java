package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.InventoryAppFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryAppFeatureRepository extends JpaRepository<InventoryAppFeature, UUID> {
    List<InventoryAppFeature> findByTenantId(UUID tenantId);
    Optional<InventoryAppFeature> findByTenantIdAndFeatureKey(UUID tenantId, String featureKey);
}
