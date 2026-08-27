package com.erp.platform.modules.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_transfer_items")
@Getter
@Setter
public class StockTransferItem extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    @JsonIgnore
    private StockTransfer stockTransfer;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;
}
