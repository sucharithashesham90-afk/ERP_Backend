package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SeasonPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeasonPeriodRepository extends JpaRepository<SeasonPeriod, UUID> {

    Page<SeasonPeriod> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<SeasonPeriod> findByTenantIdAndPeriodTypeAndDeletedAtIsNull(UUID tenantId, String periodType, Pageable pageable);

    Optional<SeasonPeriod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    java.util.List<SeasonPeriod> findByPeriodTypeIsNull();
}
