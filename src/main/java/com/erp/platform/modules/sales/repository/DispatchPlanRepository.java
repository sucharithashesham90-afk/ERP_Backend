package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.DispatchPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DispatchPlanRepository extends JpaRepository<DispatchPlan, UUID> {
    Page<DispatchPlan> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<DispatchPlan> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
