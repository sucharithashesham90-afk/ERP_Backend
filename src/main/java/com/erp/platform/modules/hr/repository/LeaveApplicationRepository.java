package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.LeaveApplication;
import com.erp.platform.modules.hr.entity.LeaveApplication.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, UUID> {

    Page<LeaveApplication> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<LeaveApplication> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, LeaveStatus status, Pageable pageable);

    Optional<LeaveApplication> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** One employee's own applications — what a non-HR user is allowed to see. */
    Page<LeaveApplication> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);

    @Query("SELECT la.leaveType, SUM(la.days) FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId AND la.employeeId = :employeeId " +
           "AND la.status = com.erp.platform.modules.hr.entity.LeaveApplication.LeaveStatus.APPROVED " +
           "AND la.deletedAt IS NULL GROUP BY la.leaveType")
    List<Object[]> sumApprovedLeaveByType(@Param("tenantId") UUID tenantId, @Param("employeeId") UUID employeeId);

    @Query("SELECT COALESCE(SUM(la.days), 0) FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId AND la.employeeId = :empId " +
           "AND la.status = com.erp.platform.modules.hr.entity.LeaveApplication.LeaveStatus.APPROVED " +
           "AND la.deletedAt IS NULL " +
           "AND la.fromDate >= :monthStart AND la.fromDate <= :monthEnd")
    Long sumApprovedLeaveForMonth(@Param("tenantId") UUID tenantId,
                                   @Param("empId") UUID empId,
                                   @Param("monthStart") LocalDate monthStart,
                                   @Param("monthEnd") LocalDate monthEnd);
}
