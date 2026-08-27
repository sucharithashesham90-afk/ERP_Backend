package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.EmployeePayDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeePayDetailRepository extends JpaRepository<EmployeePayDetail, UUID> {

    /**
     * One employee's pay details.
     *
     * <p>employee_id is a text column holding either the employee id as a string or their code,
     * depending on which screen wrote the row, so both are matched. Filtering here rather than on
     * the results means a colleague's row is never loaded.
     */
    @Query("SELECT e FROM EmployeePayDetail e WHERE e.tenantId = :tenantId AND e.employeeId IN :keys AND e.deletedAt IS NULL")
    Page<EmployeePayDetail> findOwnedBy(@Param("tenantId") UUID tenantId, @Param("keys") java.util.Collection<String> keys, Pageable pageable);

    List<EmployeePayDetail> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<EmployeePayDetail> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<EmployeePayDetail> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    /** One employee's own records — what a non-HR user is allowed to see. */
}