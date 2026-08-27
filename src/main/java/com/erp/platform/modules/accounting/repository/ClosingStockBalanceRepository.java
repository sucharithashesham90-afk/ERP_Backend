package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.ClosingStockBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClosingStockBalanceRepository extends JpaRepository<ClosingStockBalance, UUID> {
    Page<ClosingStockBalance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ClosingStockBalance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
