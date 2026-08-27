package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.FixedAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FixedAssetRepository extends JpaRepository<FixedAsset, UUID> {

    Page<FixedAsset> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<FixedAsset> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<FixedAsset> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, FixedAsset.AssetStatus status, Pageable pageable);

    List<FixedAsset> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, FixedAsset.AssetStatus status);

    List<FixedAsset> findAllByTenantIdAndDeletedAtIsNull(UUID tenantId);

    List<FixedAsset> findAllByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, FixedAsset.AssetStatus status);
}
