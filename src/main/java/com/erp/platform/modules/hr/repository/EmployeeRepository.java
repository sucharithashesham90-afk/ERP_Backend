package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /** Staff attached to one branch, for folding duplicate branches together. */
    List<Employee> findByTenantIdAndBranchIdAndDeletedAtIsNull(UUID tenantId, UUID branchId);

    Page<Employee> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Employee> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, EmployeeStatus status, Pageable pageable);

    Page<Employee> findByTenantIdAndDepartmentIdAndDeletedAtIsNull(UUID tenantId, UUID departmentId, Pageable pageable);

    Optional<Employee> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Employee> findByTenantIdAndUserIdAndDeletedAtIsNull(UUID tenantId, UUID userId);

    /**
     * By login email — the fallback for employee records created before they carried a user id.
     * Their owner should still reach their own payslip.
     */
    Optional<Employee> findFirstByTenantIdAndEmailIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String email);

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId AND e.deletedAt IS NULL AND (" +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')))")
    Page<Employee> searchByTenantId(@Param("tenantId") UUID tenantId, @Param("q") String q, Pageable pageable);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, EmployeeStatus status);

    List<Employee> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, EmployeeStatus status);
}
