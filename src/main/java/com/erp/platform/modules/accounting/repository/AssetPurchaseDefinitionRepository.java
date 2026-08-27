package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AssetPurchaseDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetPurchaseDefinitionRepository extends JpaRepository<AssetPurchaseDefinition, UUID> {
    Page<AssetPurchaseDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AssetPurchaseDefinition> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<AssetPurchaseDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
