package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, UUID> {
    List<EmployeeRole> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<EmployeeRole> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
