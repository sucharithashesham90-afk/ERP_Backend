package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessTreatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessTreatmentRepository extends JpaRepository<ProcessTreatment, UUID> {
    Page<ProcessTreatment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProcessTreatment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<ProcessTreatment> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
