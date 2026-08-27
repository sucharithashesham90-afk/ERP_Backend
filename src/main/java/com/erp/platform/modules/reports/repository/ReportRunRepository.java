package com.erp.platform.modules.reports.repository;

import com.erp.platform.modules.reports.entity.ReportRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportRunRepository extends JpaRepository<ReportRun, UUID> {

    Page<ReportRun> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ReportRun> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    Page<ReportRun> findByTenantIdAndReportDefinition_IdAndDeletedAtIsNull(UUID tenantId, UUID reportDefinitionId, Pageable pageable);
}
