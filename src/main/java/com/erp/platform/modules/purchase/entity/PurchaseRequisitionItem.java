package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_requisition_items",
       indexes = {@Index(name = "idx_pri_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PurchaseRequisitionItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    @JsonIgnore
    private PurchaseRequisition requisition;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(name = "estimated_unit_price", precision = 18, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "estimated_total_price", precision = 18, scale = 2)
    private BigDecimal estimatedTotalPrice;

    @Column(name = "preferred_vendor_id")
    private UUID preferredVendorId;

    @Column(name = "preferred_vendor_name", length = 200)
    private String preferredVendorName;

    @Column(length = 1000)
    private String specifications;

    @Column(length = 500)
    private String notes;
}
