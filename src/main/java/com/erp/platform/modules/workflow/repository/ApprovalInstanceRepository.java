package com.erp.platform.modules.workflow.repository;

import com.erp.platform.modules.workflow.entity.ApprovalInstance;
import com.erp.platform.modules.workflow.entity.ApprovalInstance.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstance, UUID> {

    Page<ApprovalInstance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ApprovalInstance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    /** Approvals still waiting on someone — the dashboard's "Pending Approvals" figure. */
    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ApprovalStatus status);

    Page<ApprovalInstance> findByTenantIdAndDocumentTypeAndDeletedAtIsNull(UUID tenantId, String documentType, Pageable pageable);

    Optional<ApprovalInstance> findByTenantIdAndDocumentIdAndDeletedAtIsNull(UUID tenantId, UUID documentId);

    Page<ApprovalInstance> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ApprovalStatus status, Pageable pageable);
}
