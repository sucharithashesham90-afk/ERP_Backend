package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PackingMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackingMaterialRepository extends JpaRepository<PackingMaterial, UUID> {
    Page<PackingMaterial> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<PackingMaterial> findByTenantIdAndProductIdIsNullAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<PackingMaterial> findByTenantIdAndProductIdIsNotNullAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PackingMaterial> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
