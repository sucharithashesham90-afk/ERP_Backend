package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.DemandForecast;
import com.erp.platform.modules.sales.entity.DemandForecast.ForecastStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DemandForecastRepository extends JpaRepository<DemandForecast, UUID> {

    Page<DemandForecast> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DemandForecast> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<DemandForecast> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);

    Page<DemandForecast> findByTenantIdAndForecastYearAndDeletedAtIsNull(UUID tenantId, int forecastYear, Pageable pageable);

    Page<DemandForecast> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ForecastStatus status, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
