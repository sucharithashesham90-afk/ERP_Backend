package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AssetPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetPurchaseRepository extends JpaRepository<AssetPurchase, UUID> {
    Page<AssetPurchase> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<AssetPurchase> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
