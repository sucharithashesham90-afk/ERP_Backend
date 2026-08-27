package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessLoss;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessLossRepository extends JpaRepository<ProcessLoss, UUID> {
    Page<ProcessLoss> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProcessLoss> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    Optional<ProcessLoss> findByTenantIdAndProcessStepIdAndDeletedAtIsNull(UUID tenantId, UUID processStepId);
}
