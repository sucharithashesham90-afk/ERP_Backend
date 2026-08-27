package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.TadaClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TadaClaimRepository extends JpaRepository<TadaClaim, UUID> {

    /**
     * One employee's TA/DA claims.
     *
     * <p>employee_id is a text column holding either the employee id as a string or their code,
     * depending on which screen wrote the row, so both are matched. Filtering here rather than on
     * the results means a colleague's row is never loaded.
     */
    @Query("SELECT e FROM TadaClaim e WHERE e.tenantId = :tenantId AND e.employeeId IN :keys AND e.deletedAt IS NULL")
    Page<TadaClaim> findOwnedBy(@Param("tenantId") UUID tenantId, @Param("keys") java.util.Collection<String> keys, Pageable pageable);

    Page<TadaClaim> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<TadaClaim> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** One employee's own records — what a non-HR user is allowed to see. */
}