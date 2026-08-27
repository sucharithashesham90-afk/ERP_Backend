package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.FiscalYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, UUID> {
    Page<FiscalYear> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<FiscalYear> findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(UUID tenantId);
    Optional<FiscalYear> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndPeriodCodeAndDeletedAtIsNull(UUID tenantId, String periodCode);
}
