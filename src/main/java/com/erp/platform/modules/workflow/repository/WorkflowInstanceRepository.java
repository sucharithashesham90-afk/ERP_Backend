package com.erp.platform.modules.workflow.repository;

import com.erp.platform.modules.workflow.entity.WorkflowInstance;
import com.erp.platform.modules.workflow.entity.WorkflowInstance.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {

    Page<WorkflowInstance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<WorkflowInstance> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, WorkflowStatus status, Pageable pageable);

    Optional<WorkflowInstance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
            UUID tenantId, UUID referenceId, String module, WorkflowStatus status);
}
