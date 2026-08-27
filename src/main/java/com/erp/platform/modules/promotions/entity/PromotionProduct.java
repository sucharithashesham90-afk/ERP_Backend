package com.erp.platform.modules.promotions.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "promotion_products",
        indexes = {
                @Index(name = "idx_promo_prod_promo", columnList = "promotion_id")
        })
@Getter
@Setter
public class PromotionProduct extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    @JsonIgnore
    private Promotion promotion;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "min_quantity", precision = 18, scale = 3)
    private BigDecimal minQuantity;

    @Column(name = "discount_percent", precision = 10, scale = 2)
    private BigDecimal discountPercent;
}
