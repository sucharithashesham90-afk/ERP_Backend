package com.erp.platform.modules.workflow.repository;

import com.erp.platform.modules.workflow.entity.ApprovalStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, UUID> {

    Page<ApprovalStep> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ApprovalStep> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    List<ApprovalStep> findByTenantIdAndInstance_IdAndDeletedAtIsNull(UUID tenantId, UUID instanceId);
}
