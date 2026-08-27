package com.erp.platform.modules.auth.repository;

import com.erp.platform.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    List<Role> findBySystemTrue();
    List<Role> findByTenantId(UUID tenantId);
    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT r FROM Role r WHERE r.system = true OR r.tenantId = :tenantId")
    List<Role> findAllForTenant(@Param("tenantId") UUID tenantId);
}
