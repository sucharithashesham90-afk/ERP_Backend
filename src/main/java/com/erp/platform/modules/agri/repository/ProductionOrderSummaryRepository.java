package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProductionOrderSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProductionOrderSummaryRepository extends JpaRepository<ProductionOrderSummary, UUID> {
    Page<ProductionOrderSummary> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProductionOrderSummary> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
