package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.MaterialGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialGroupRepository extends JpaRepository<MaterialGroup, UUID> {
    List<MaterialGroup> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<MaterialGroup> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
