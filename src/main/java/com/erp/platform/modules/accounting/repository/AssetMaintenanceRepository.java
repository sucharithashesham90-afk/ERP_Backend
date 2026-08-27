package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AssetMaintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, UUID> {

    Page<AssetMaintenance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<AssetMaintenance> findByTenantIdAndAssetIdAndDeletedAtIsNull(UUID tenantId, UUID assetId, Pageable pageable);

    Optional<AssetMaintenance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<AssetMaintenance> findByTenantIdAndAssetIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(UUID tenantId, UUID assetId);
}
