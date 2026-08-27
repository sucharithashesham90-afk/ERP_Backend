package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.LotStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotStockRepository extends JpaRepository<LotStock, UUID>, JpaSpecificationExecutor<LotStock> {

    Page<LotStock> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<LotStock> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<LotStock> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);

    List<LotStock> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    Page<LotStock> findByTenantIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID warehouseId, Pageable pageable);

    Page<LotStock> findByTenantIdAndProductIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID productId, UUID warehouseId, Pageable pageable);

    boolean existsByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID productId, LotStock.LotStockStatus status);

    // Unpaged reads backing FEFO allocation and the expiry overview, which both need every
    // candidate lot in one pass to order and bucket them.
    List<LotStock> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    List<LotStock> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId);
}
