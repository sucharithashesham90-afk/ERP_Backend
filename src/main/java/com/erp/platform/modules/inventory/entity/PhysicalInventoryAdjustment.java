package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Physical inventory count adjustment. Backs /api/v1/inventory/physical-inventory/adjust. */
@Entity
@Table(name = "physical_inventory_adjustments",
       indexes = {@Index(name = "idx_physadj_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PhysicalInventoryAdjustment extends TenantEntity {

    /** Identifier of the stock row being adjusted (opaque — may be UUID or composite). */
    @Column(name = "stock_id", length = 100)
    private String stockId;

    @Column(precision = 18, scale = 4)
    private BigDecimal adjustment = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @Column(name = "adjusted_by", length = 200)
    private String adjustedBy;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;
}
