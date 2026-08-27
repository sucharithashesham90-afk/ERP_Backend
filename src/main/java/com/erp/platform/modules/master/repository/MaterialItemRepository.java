package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.MaterialItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialItemRepository extends JpaRepository<MaterialItem, UUID> {

    Page<MaterialItem> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<MaterialItem> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    List<MaterialItem> findByTenantIdAndMaterialIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID materialId);

    Optional<MaterialItem> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndMaterialIdAndCodeAndDeletedAtIsNull(UUID tenantId, UUID materialId, String code);
}
