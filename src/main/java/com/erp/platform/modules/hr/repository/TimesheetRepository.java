package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    Page<Timesheet> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    /** One employee's entries only — the filter belongs in the query, not applied to the results. */
    Page<Timesheet> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);

    Optional<Timesheet> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
