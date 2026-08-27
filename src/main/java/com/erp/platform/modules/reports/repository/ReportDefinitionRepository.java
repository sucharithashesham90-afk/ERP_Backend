package com.erp.platform.modules.reports.repository;

import com.erp.platform.modules.reports.entity.ReportDefinition;
import com.erp.platform.modules.reports.entity.ReportDefinition.ReportCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, UUID> {

    Page<ReportDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ReportDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    Page<ReportDefinition> findByTenantIdAndReportCategoryAndDeletedAtIsNull(UUID tenantId, ReportCategory category, Pageable pageable);

    Page<ReportDefinition> findByTenantIdAndIsPublicAndDeletedAtIsNull(UUID tenantId, boolean isPublic, Pageable pageable);

    Page<ReportDefinition> findByTenantIdAndCreatedByAndDeletedAtIsNull(UUID tenantId, String createdBy, Pageable pageable);
}
