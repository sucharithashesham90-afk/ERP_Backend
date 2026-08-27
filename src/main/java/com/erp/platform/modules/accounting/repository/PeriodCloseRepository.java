package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.PeriodClose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodCloseRepository extends JpaRepository<PeriodClose, UUID> {

    Page<PeriodClose> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PeriodClose> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<PeriodClose> findByTenantIdAndPeriodYearAndDeletedAtIsNull(UUID tenantId, int periodYear, Pageable pageable);

    Optional<PeriodClose> findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(UUID tenantId, int periodYear, int periodMonth);
}
