package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseAppFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseAppFeatureRepository extends JpaRepository<PurchaseAppFeature, UUID> {
    List<PurchaseAppFeature> findByTenantId(UUID tenantId);
    Optional<PurchaseAppFeature> findByTenantIdAndFeatureKey(UUID tenantId, String featureKey);
}
