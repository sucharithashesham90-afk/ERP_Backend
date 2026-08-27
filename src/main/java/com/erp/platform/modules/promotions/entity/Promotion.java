package com.erp.platform.modules.promotions.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotions",
        indexes = {
                @Index(name = "idx_promo_tenant", columnList = "tenant_id"),
                @Index(name = "idx_promo_active", columnList = "tenant_id, active"),
                @Index(name = "idx_promo_dates", columnList = "tenant_id, start_date, end_date")
        })
@Getter
@Setter
public class Promotion extends TenantEntity {

    @Column(name = "promotion_code", nullable = false, length = 50)
    private String promotionCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", length = 30)
    private PromotionType promotionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to", length = 30)
    private ApplicableTo applicableTo;

    @Column(name = "customer_category", length = 50)
    private String customerCategory;

    @Column(name = "discount_percent", precision = 10, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "min_order_value", precision = 18, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "min_order_qty", precision = 18, scale = 3)
    private BigDecimal minOrderQty = BigDecimal.ZERO;

    @Column(name = "buy_quantity")
    private int buyQuantity;

    @Column(name = "get_quantity")
    private int getQuantity;

    @Column(name = "free_product_id")
    private UUID freeProductId;

    @Column(name = "free_product_name", length = 200)
    private String freeProductName;

    @Column(name = "max_discount_amount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "usage_limit")
    private int usageLimit = 0;

    @Column(name = "usage_count")
    private int usageCount = 0;

    @Column
    private boolean stackable = false;

    @Column
    private boolean active = true;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> applicableProducts = new ArrayList<>();

    public enum PromotionType {
        PERCENTAGE_DISCOUNT, FIXED_DISCOUNT, BUY_X_GET_Y, FREE_PRODUCT, BUNDLE, CASHBACK
    }

    public enum ApplicableTo {
        ALL_CUSTOMERS, SPECIFIC_CATEGORY, SPECIFIC_CUSTOMER, CHANNEL
    }
}
