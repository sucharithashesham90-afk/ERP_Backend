package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_quotation_items",
       indexes = {@Index(name = "idx_pqi_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PurchaseQuotationItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    @JsonIgnore
    private PurchaseQuotation quotation;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

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

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_percent", precision = 6, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "delivery_days")
    private int deliveryDays;

    @Column(length = 500)
    private String remarks;
}
