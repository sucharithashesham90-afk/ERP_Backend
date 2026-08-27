package com.erp.platform.modules.promotions.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "promotion_redemptions",
        indexes = {
                @Index(name = "idx_promo_redemption_promo", columnList = "promotion_id"),
                @Index(name = "idx_promo_redemption_customer", columnList = "customer_id")
        })
@Getter
@Setter
public class PromotionRedemption extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "redemption_date")
    private LocalDate redemptionDate;

    @Column(name = "discount_applied", precision = 18, scale = 2)
    private BigDecimal discountApplied;

    @Column(length = 500)
    private String notes;
}
