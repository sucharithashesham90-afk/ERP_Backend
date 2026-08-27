package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AssetGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetGroupRepository extends JpaRepository<AssetGroup, UUID> {

    Page<AssetGroup> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<AssetGroup> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<AssetGroup> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);
}
