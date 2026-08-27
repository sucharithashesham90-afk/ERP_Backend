package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.CustomerStockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerStockTransferRepository extends JpaRepository<CustomerStockTransfer, UUID> {

    Page<CustomerStockTransfer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CustomerStockTransfer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
