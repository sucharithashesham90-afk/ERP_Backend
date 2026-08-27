package com.erp.platform.modules.intake.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "intake_slip_items",
       indexes = {
           @Index(name = "idx_isli_tenant", columnList = "tenant_id"),
           @Index(name = "idx_isli_slip", columnList = "slip_id")
       })
@Getter
@Setter
public class IntakeSlipItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slip_id", nullable = false)
    @JsonIgnore
    private IntakeSlip slip;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "received_quantity", precision = 18, scale = 4)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "accepted_quantity", precision = 18, scale = 4)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", precision = 18, scale = 4)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "quality_remarks", length = 500)
    private String qualityRemarks;
}
