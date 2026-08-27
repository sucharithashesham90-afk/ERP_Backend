package com.erp.platform.modules.workflow.repository;

import com.erp.platform.modules.workflow.entity.WorkflowDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    Page<WorkflowDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<WorkflowDefinition> findByTenantIdAndModuleAndActiveTrueAndDeletedAtIsNull(UUID tenantId, String module);

    Optional<WorkflowDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
