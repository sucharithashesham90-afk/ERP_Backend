package com.erp.platform.modules.intake.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "intake_schedule_items",
       indexes = {
           @Index(name = "idx_isi_tenant", columnList = "tenant_id"),
           @Index(name = "idx_isi_schedule", columnList = "schedule_id")
       })
@Getter
@Setter
public class IntakeScheduleItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private IntakeSchedule schedule;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "expected_quantity", precision = 18, scale = 4)
    private BigDecimal expectedQuantity = BigDecimal.ZERO;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(length = 1000)
    private String notes;
}
