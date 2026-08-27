package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "advance_adjustments",
       indexes = {
           @Index(name = "idx_advadj_tenant", columnList = "tenant_id"),
           @Index(name = "idx_advadj_advance", columnList = "advance_id")
       })
@Getter
@Setter
public class AdvanceAdjustment extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SupplierAdvance advance;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "adjustment_date", nullable = false)
    private LocalDate adjustmentDate;

    @Column(name = "adjusted_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal adjustedAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;
}
