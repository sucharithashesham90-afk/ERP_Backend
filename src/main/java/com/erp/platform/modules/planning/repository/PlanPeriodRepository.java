package com.erp.platform.modules.planning.repository;

import com.erp.platform.modules.planning.entity.PlanPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanPeriodRepository extends JpaRepository<PlanPeriod, UUID> {
    List<PlanPeriod> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<PlanPeriod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
