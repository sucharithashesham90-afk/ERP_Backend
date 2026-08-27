package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesPlan;
import com.erp.platform.modules.sales.entity.SalesPlan.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesPlanRepository extends JpaRepository<SalesPlan, UUID> {

    Page<SalesPlan> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SalesPlan> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SalesPlan> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, PlanStatus status, Pageable pageable);

    Page<SalesPlan> findByTenantIdAndPlanYearAndDeletedAtIsNull(UUID tenantId, int planYear, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
