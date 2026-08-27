package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_items",
       indexes = {@Index(name = "idx_stock_tenant", columnList = "tenant_id")},
       uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id", "material_state", "material_type"}))
@Getter
@Setter
public class StockItem extends TenantEntity {

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "quantity_on_hand", precision = 18, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "quantity_reserved", precision = 18, scale = 4)
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @Column(name = "average_cost", precision = 18, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(length = 100)
    private String location;

    /** Material processing state: RAW, DRIED, GRADED, TREATED, PACKED. */
    @Column(name = "material_state", length = 30)
    private String materialState = "RAW";

    /** Material quality/condition type: GOOD, DAMAGED, EXPIRED, RETURNED, SMALLSEED. */
    @Column(name = "material_type", length = 30)
    private String materialType = "GOOD";

    // Convenience getter for available quantity
    public BigDecimal getQuantityAvailable() {
        return quantityOnHand.subtract(quantityReserved);
    }
}
