package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.LocationStockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationStockTransferRepository extends JpaRepository<LocationStockTransfer, UUID> {
    Page<LocationStockTransfer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<LocationStockTransfer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
