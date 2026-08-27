package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BudgetEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetEntryRepository extends JpaRepository<BudgetEntry, UUID> {

    Page<BudgetEntry> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<BudgetEntry> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<BudgetEntry> findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(UUID tenantId, int periodYear, int periodMonth, Pageable pageable);

    List<BudgetEntry> findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(UUID tenantId, int periodYear, int periodMonth);
}
