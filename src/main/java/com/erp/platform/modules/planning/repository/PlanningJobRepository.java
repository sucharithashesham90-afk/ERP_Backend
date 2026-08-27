package com.erp.platform.modules.planning.repository;

import com.erp.platform.modules.planning.entity.ProductionJob;
import com.erp.platform.modules.planning.entity.ProductionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanningJobRepository extends JpaRepository<ProductionJob, UUID> {

    Page<ProductionJob> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<ProductionJob> findByTenantIdAndPlanAndDeletedAtIsNull(UUID tenantId, ProductionPlan plan, Pageable pageable);

    Optional<ProductionJob> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
