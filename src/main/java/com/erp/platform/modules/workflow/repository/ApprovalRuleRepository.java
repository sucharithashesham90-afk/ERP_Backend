package com.erp.platform.modules.workflow.repository;

import com.erp.platform.modules.workflow.entity.ApprovalRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, UUID> {

    Page<ApprovalRule> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ApprovalRule> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    Page<ApprovalRule> findByTenantIdAndDocumentTypeAndDeletedAtIsNull(UUID tenantId, String documentType, Pageable pageable);

    List<ApprovalRule> findByTenantIdAndDocumentTypeAndActiveAndDeletedAtIsNull(UUID tenantId, String documentType, boolean active);
}
