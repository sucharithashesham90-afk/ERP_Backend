package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    Page<StockTransfer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<StockTransfer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
