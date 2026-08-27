package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessTemplateStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessTemplateStepRepository extends JpaRepository<ProcessTemplateStep, UUID> {

    List<ProcessTemplateStep> findByTemplateIdOrderBySequenceNumber(UUID templateId);

    Optional<ProcessTemplateStep> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTemplateId(UUID templateId);
}
