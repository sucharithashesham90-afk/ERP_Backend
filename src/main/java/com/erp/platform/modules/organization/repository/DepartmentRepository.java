package com.erp.platform.modules.organization.repository;

import com.erp.platform.modules.organization.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Page<Department> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<Department> findByTenantIdAndCompanyIdAndDeletedAtIsNull(UUID tenantId, UUID companyId, Pageable pageable);
    Optional<Department> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
