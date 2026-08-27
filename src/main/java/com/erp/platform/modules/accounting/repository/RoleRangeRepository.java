package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.RoleRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRangeRepository extends JpaRepository<RoleRange, UUID> {
    Page<RoleRange> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<RoleRange> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<RoleRange> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    Optional<RoleRange> findByTenantIdAndRoleNameAndDeletedAtIsNull(UUID tenantId, String roleName);
}
