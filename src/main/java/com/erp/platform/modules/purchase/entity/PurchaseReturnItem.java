package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_return_items")
@Getter
@Setter
public class PurchaseReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_return_id", nullable = false)
    @JsonIgnore
    private PurchaseReturn purchaseReturn;

    @Column(name = "goods_receipt_item_id")
    private UUID goodsReceiptItemId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    // Material details carried over from the source document, so a saved return still
    // shows what was actually sent back when it is reopened for approval.
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

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(length = 50)
    private String unit;

    @Column(name = "return_qty", precision = 18, scale = 3)
    private BigDecimal returnQty = BigDecimal.ZERO;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 500)
    private String reason;
}
