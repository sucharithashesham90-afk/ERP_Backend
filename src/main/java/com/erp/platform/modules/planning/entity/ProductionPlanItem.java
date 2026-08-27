package com.erp.platform.modules.planning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "production_plan_items",
       indexes = {
           @Index(name = "idx_ppi_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ppi_plan", columnList = "plan_id")
       })
@Getter
@Setter
public class ProductionPlanItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private ProductionPlan plan;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "target_quantity", precision = 18, scale = 4)
    private BigDecimal targetQuantity = BigDecimal.ZERO;

    @Column(name = "produced_quantity", precision = 18, scale = 4)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;

    @Column(name = "priority_order")
    private int priorityOrder;

    @Column(length = 1000)
    private String notes;
}
