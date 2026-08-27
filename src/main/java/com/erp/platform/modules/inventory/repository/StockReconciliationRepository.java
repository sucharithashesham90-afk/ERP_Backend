package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockReconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface StockReconciliationRepository extends JpaRepository<StockReconciliation, UUID> {
    Page<StockReconciliation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<StockReconciliation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
