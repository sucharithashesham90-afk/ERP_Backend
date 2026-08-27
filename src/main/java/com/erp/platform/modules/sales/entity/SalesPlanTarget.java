package com.erp.platform.modules.sales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_plan_targets",
       indexes = {
           @Index(name = "idx_spt_tenant", columnList = "tenant_id"),
           @Index(name = "idx_spt_plan", columnList = "tenant_id, sales_plan_id")
       })
@Getter
@Setter
public class SalesPlanTarget extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_plan_id", nullable = false)
    @JsonIgnore
    private SalesPlan salesPlan;

    /** What the target is for: a variety, reached through its crop group and crop. */
    @Column(name = "crop_group_id")
    private UUID cropGroupId;

    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_id")
    private UUID cropId;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_id")
    private UUID varietyId;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "target_quantity", precision = 18, scale = 4)
    private BigDecimal targetQuantity = BigDecimal.ZERO;

    @Column(name = "target_revenue", precision = 18, scale = 2)
    private BigDecimal targetRevenue = BigDecimal.ZERO;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(name = "actual_revenue", precision = 18, scale = 2)
    private BigDecimal actualRevenue = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;
}
