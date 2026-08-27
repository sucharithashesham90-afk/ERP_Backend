package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PackagingMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PackagingMaterialRepository extends JpaRepository<PackagingMaterial, UUID> {
    Page<PackagingMaterial> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PackagingMaterial> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
