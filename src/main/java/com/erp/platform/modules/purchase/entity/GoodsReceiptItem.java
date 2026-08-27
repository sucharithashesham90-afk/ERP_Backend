package com.erp.platform.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "goods_receipt_items")
@Getter
@Setter
public class GoodsReceiptItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    @JsonIgnore
    private GoodsReceipt goodsReceipt;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "ordered_qty", precision = 18, scale = 4)
    private BigDecimal orderedQty = BigDecimal.ZERO;

    @Column(name = "received_qty", precision = 18, scale = 4)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(name = "accepted_qty", precision = 18, scale = 4)
    private BigDecimal acceptedQty = BigDecimal.ZERO;

    @Column(name = "rejected_qty", precision = 18, scale = 4)
    private BigDecimal rejectedQty = BigDecimal.ZERO;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;
}
