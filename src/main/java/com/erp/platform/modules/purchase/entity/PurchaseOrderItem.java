package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
public class PurchaseOrderItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    // SEED items use crop group + variety (not a product) and a bags x qty/bag breakdown.
    @Column(name = "item_type", length = 30)
    private String itemType;

    @Column(name = "crop_group_name", length = 200)
    private String cropGroupName;

    @Column(name = "crop_name", length = 200)
    private String cropName;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "number_of_bags")
    private Integer numberOfBags;

    @Column(name = "quantity_per_bag", precision = 18, scale = 4)
    private BigDecimal quantityPerBag;

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

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 8, scale = 4)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "received_qty", precision = 18, scale = 4)
    private BigDecimal receivedQty = BigDecimal.ZERO;
}
