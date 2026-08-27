package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lot_stock",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_lot_stock_warehouse_lot_product",
                             columnNames = {"warehouse_id", "lot_number", "product_id"})
       },
       indexes = {
           @Index(name = "idx_lot_stock_tenant", columnList = "tenant_id"),
           @Index(name = "idx_lot_stock_product", columnList = "tenant_id, product_id"),
           @Index(name = "idx_lot_stock_warehouse", columnList = "tenant_id, warehouse_id"),
           @Index(name = "idx_lot_stock_lot_number", columnList = "tenant_id, lot_number")
       })
@Getter
@Setter
public class LotStock extends TenantEntity {

    @Column(name = "lot_number", nullable = false, length = 100)
    private String lotNumber;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "storage_location_id")
    private UUID storageLocationId;

    @Column(name = "storage_location_name", length = 200)
    private String storageLocationName;

    @Column(name = "source_type", length = 20)
    @Enumerated(EnumType.STRING)
    private LotSource sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity_on_hand", precision = 18, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "quantity_reserved", precision = 18, scale = 4)
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LotStockStatus status = LotStockStatus.AVAILABLE;

    public enum LotSource {
        INTAKE, PRODUCTION, OPENING
    }

    public enum LotStockStatus {
        AVAILABLE, QUARANTINE, RELEASED, EXHAUSTED, EXPIRED
    }
}
