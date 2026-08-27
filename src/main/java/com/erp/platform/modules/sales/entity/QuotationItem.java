package com.erp.platform.modules.sales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quotation_items")
@Getter
@Setter
public class QuotationItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    @JsonIgnore
    private Quotation quotation;

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

    @Column(length = 500)
    private String description;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 8, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 8, scale = 4)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
