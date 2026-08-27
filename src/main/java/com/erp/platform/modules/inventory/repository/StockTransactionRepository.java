package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockMovement, UUID>, JpaSpecificationExecutor<StockMovement> {

    Page<StockMovement> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<StockMovement> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);

    Page<StockMovement> findByTenantIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID warehouseId, Pageable pageable);

    @Query("SELECT m FROM StockMovement m WHERE m.tenantId = :tenantId AND m.productId = :productId " +
           "AND m.movementDate < :before AND m.deletedAt IS NULL " +
           "ORDER BY m.movementDate DESC, m.createdAt DESC")
    List<StockMovement> findLastMovementBefore(@Param("tenantId") UUID tenantId,
                                               @Param("productId") UUID productId,
                                               @Param("before") LocalDate before,
                                               Pageable pageable);

    @Query("SELECT m FROM StockMovement m WHERE m.tenantId = :tenantId AND m.productId = :productId " +
           "AND m.warehouseId = :warehouseId AND m.movementDate < :before AND m.deletedAt IS NULL " +
           "ORDER BY m.movementDate DESC, m.createdAt DESC")
    List<StockMovement> findLastMovementBeforeByWarehouse(@Param("tenantId") UUID tenantId,
                                                          @Param("productId") UUID productId,
                                                          @Param("warehouseId") UUID warehouseId,
                                                          @Param("before") LocalDate before,
                                                          Pageable pageable);
}
