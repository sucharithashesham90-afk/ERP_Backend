package com.erp.platform.modules.planning.repository;

import com.erp.platform.modules.planning.entity.ProductionPlan;
import com.erp.platform.modules.planning.entity.ProductionPlan.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, UUID> {

    Page<ProductionPlan> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<ProductionPlan> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, PlanStatus status, Pageable pageable);

    Optional<ProductionPlan> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
