package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.WfhRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WfhRequestRepository extends JpaRepository<WfhRequest, UUID> {

    /** One employee's rows only — the filter belongs in the query, not applied to the results. */
    Page<WfhRequest> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);
    Page<WfhRequest> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<WfhRequest> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
