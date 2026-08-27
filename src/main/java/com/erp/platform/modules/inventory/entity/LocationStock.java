package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "location_stock",
       uniqueConstraints = {@UniqueConstraint(name = "uk_location_stock_loc_product",
                                              columnNames = {"location_id", "product_id", "lot_number"})},
       indexes = {@Index(name = "idx_loc_stock_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_loc_stock_location", columnList = "tenant_id, location_id"),
                  @Index(name = "idx_loc_stock_product", columnList = "tenant_id, product_id")})
@Getter
@Setter
public class LocationStock extends TenantEntity {

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "location_code", length = 50)
    private String locationCode;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 300)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", precision = 18, scale = 4)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", precision = 18, scale = 4)
    private BigDecimal unitCost;
}
