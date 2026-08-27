package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, UUID> {
    Page<EmployeeStatus> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<EmployeeStatus> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<EmployeeStatus> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
