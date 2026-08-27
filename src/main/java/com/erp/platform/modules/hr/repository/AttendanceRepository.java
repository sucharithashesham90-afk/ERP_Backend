package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Page<Attendance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Attendance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Attendance> findByTenantIdAndEmployeeIdAndDate(UUID tenantId, UUID empId, LocalDate date);

    Optional<Attendance> findByTenantIdAndEmployeeIdAndDateAndDeletedAtIsNull(UUID tenantId, UUID empId, LocalDate date);

    List<Attendance> findByTenantIdAndEmployeeIdAndDateBetween(UUID tenantId, UUID empId, LocalDate from, LocalDate to);

    List<Attendance> findByTenantIdAndDate(UUID tenantId, LocalDate date);

    /** One employee's own records — what a non-HR user is allowed to see. */
    org.springframework.data.domain.Page<Attendance> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);
}