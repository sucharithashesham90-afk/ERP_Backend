package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    /** Returns the first matching stock item across all material dimensions for a warehouse+product; safe even when multiple dimension rows exist. */
    Optional<StockItem> findFirstByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    Optional<StockItem> findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(
            UUID warehouseId, UUID productId, String materialState, String materialType);

    Optional<StockItem> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<StockItem> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<StockItem> findByTenantIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID warehouseId, Pageable pageable);

    @Query("SELECT s FROM StockItem s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL AND s.quantityOnHand <= :threshold")
    List<StockItem> findLowStock(@Param("tenantId") UUID tenantId, @Param("threshold") BigDecimal threshold);

    @Query("SELECT COALESCE(SUM(s.quantityOnHand), 0) FROM StockItem s WHERE s.tenantId = :tenantId AND s.productId = :productId AND s.deletedAt IS NULL")
    BigDecimal sumQuantityByTenantIdAndProductId(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);

    List<StockItem> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId);
}
