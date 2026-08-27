package com.erp.platform.modules.payroll.repository;

import com.erp.platform.modules.payroll.entity.Payslip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    Page<Payslip> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Payslip> findByTenantIdAndPayMonthAndPayYearAndDeletedAtIsNull(
            UUID tenantId, int month, int year, Pageable pageable);

    Optional<Payslip> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** One employee's own payslips — what a non-HR user is allowed to see. */
    Page<Payslip> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);

    Page<Payslip> findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
            UUID tenantId, UUID employeeId, int month, int year, Pageable pageable);

    Optional<Payslip> findByTenantIdAndEmployeeIdAndPayMonthAndPayYearAndDeletedAtIsNull(
            UUID tenantId, UUID empId, int month, int year);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payslip p " +
           "WHERE p.tenantId = :tenantId AND p.payMonth = :month AND p.payYear = :year " +
           "AND p.status = 'PAID' AND p.deletedAt IS NULL")
    BigDecimal sumNetSalaryByMonthYear(@Param("tenantId") UUID tenantId,
                                       @Param("month") int month,
                                       @Param("year") int year);
}
