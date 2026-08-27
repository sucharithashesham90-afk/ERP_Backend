package com.erp.platform.modules.sales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
public class InvoiceItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

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

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "pack_type", length = 100)
    private String packType;

    @Column(name = "pack_size", precision = 10, scale = 3)
    private BigDecimal packSize = BigDecimal.ZERO;

    @Column(name = "forwarding_charges", precision = 18, scale = 4)
    private BigDecimal forwardingCharges = BigDecimal.ZERO;

    @Column(name = "surcharge_percent", precision = 8, scale = 4)
    private BigDecimal surchargePercent = BigDecimal.ZERO;

    @Column(name = "surcharge_amount", precision = 18, scale = 2)
    private BigDecimal surchargeAmount = BigDecimal.ZERO;
}
