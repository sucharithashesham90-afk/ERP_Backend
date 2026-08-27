package com.erp.platform.modules.promotions.dto;

import com.erp.platform.modules.promotions.entity.Promotion.ApplicableTo;
import com.erp.platform.modules.promotions.entity.Promotion.PromotionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PromotionDto {

    private UUID id;
    private UUID tenantId;
    private String promotionCode;
    private String name;
    private String description;
    private PromotionType promotionType;
    private ApplicableTo applicableTo;
    private String customerCategory;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minOrderValue;
    private BigDecimal minOrderQty;
    private int buyQuantity;
    private int getQuantity;
    private UUID freeProductId;
    private String freeProductName;
    private BigDecimal maxDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private int usageLimit;
    private int usageCount;
    private boolean stackable;
    private boolean active;
    private String notes;
    private List<ProductDto> applicableProducts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ProductDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal minQuantity;
        private BigDecimal discountPercent;
    }
}
