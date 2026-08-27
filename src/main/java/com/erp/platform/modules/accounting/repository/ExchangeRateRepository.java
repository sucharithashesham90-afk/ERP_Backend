package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Page<ExchangeRate> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ExchangeRate> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT er FROM ExchangeRate er WHERE er.tenantId = :tenantId " +
           "AND er.baseCurrency = :base AND er.targetCurrency = :target " +
           "AND er.effectiveDate <= :asOf AND er.deletedAt IS NULL " +
           "ORDER BY er.effectiveDate DESC")
    Page<ExchangeRate> findLatestRate(@Param("tenantId") UUID tenantId,
                                      @Param("base") String base,
                                      @Param("target") String target,
                                      @Param("asOf") LocalDate asOf,
                                      Pageable pageable);
}
