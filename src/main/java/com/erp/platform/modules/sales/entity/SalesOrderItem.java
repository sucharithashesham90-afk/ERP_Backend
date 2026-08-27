package com.erp.platform.modules.sales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_order_items")
@Getter
@Setter
public class SalesOrderItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonIgnore
    private SalesOrder salesOrder;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

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

    @Column(name = "delivered_qty", precision = 18, scale = 4)
    private BigDecimal deliveredQty = BigDecimal.ZERO;
}
