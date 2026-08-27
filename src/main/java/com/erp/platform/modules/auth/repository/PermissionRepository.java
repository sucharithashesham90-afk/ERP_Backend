package com.erp.platform.modules.auth.repository;

import com.erp.platform.modules.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    List<Permission> findByModuleKey(String moduleKey);
    boolean existsByName(String name);
}
