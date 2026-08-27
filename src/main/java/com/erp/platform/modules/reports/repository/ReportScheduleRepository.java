package com.erp.platform.modules.reports.repository;

import com.erp.platform.modules.reports.entity.ReportSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, UUID> {

    Page<ReportSchedule> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ReportSchedule> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);
}
