package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesPlan;
import com.erp.platform.modules.sales.entity.SalesPlanTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesPlanTargetRepository extends JpaRepository<SalesPlanTarget, UUID> {

    Page<SalesPlanTarget> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SalesPlanTarget> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<SalesPlanTarget> findByTenantIdAndSalesPlanAndDeletedAtIsNull(UUID tenantId, SalesPlan salesPlan);
}
