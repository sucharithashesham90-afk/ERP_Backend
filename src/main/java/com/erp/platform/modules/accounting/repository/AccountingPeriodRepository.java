package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AccountingPeriod;
import com.erp.platform.modules.accounting.entity.AccountingPeriod.PeriodStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, UUID> {

    Page<AccountingPeriod> findByTenantIdAndDeletedAtIsNullOrderByPeriodYearDescPeriodMonthDesc(
            UUID tenantId, Pageable pageable);

    Optional<AccountingPeriod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<AccountingPeriod> findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(
            UUID tenantId, int year, int month);

    Optional<AccountingPeriod> findByTenantIdAndPeriodYearAndPeriodMonthAndStatusAndDeletedAtIsNull(
            UUID tenantId, int year, int month, PeriodStatus status);
}
