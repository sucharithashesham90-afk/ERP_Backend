package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessBOM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessBOMRepository extends JpaRepository<ProcessBOM, UUID> {
    Page<ProcessBOM> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProcessBOM> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<ProcessBOM> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
