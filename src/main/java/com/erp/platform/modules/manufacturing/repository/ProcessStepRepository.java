package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, UUID> {

    Page<ProcessStep> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProcessStep> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<ProcessStep> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);
}
