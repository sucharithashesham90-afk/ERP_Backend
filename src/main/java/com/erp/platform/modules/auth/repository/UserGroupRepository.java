package com.erp.platform.modules.auth.repository;

import com.erp.platform.modules.auth.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    List<UserGroup> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<UserGroup> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndNameAndDeletedAtIsNull(UUID tenantId, String name);
}
