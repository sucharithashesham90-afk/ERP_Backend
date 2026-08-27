package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.RoleHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, UUID> {
    List<RoleHierarchy> findByTenantIdAndDeletedAtIsNullOrderByLevelOrder(UUID tenantId);
    Optional<RoleHierarchy> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndRoleNameAndDeletedAtIsNull(UUID tenantId, String roleName);
}
