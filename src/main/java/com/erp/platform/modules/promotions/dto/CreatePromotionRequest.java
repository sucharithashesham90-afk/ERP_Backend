package com.erp.platform.modules.promotions.dto;

import com.erp.platform.modules.promotions.entity.Promotion.ApplicableTo;
import com.erp.platform.modules.promotions.entity.Promotion.PromotionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePromotionRequest {

    @NotBlank(message = "Promotion name is required")
    private String name;

    private String description;

    @NotNull(message = "Promotion type is required")
    private PromotionType promotionType;

    @NotNull(message = "Applicable to is required")
    private ApplicableTo applicableTo;

    private String customerCategory;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    private BigDecimal minOrderQty = BigDecimal.ZERO;
    private int buyQuantity;
    private int getQuantity;
    private UUID freeProductId;
    private String freeProductName;
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private int usageLimit = 0;
    private boolean stackable = false;
    private boolean active = true;
    private String notes;

    @Valid
    private List<ProductRequest> applicableProducts;

    @Data
    public static class ProductRequest {
        private UUID productId;
        private String productName;
        private BigDecimal minQuantity;
        private BigDecimal discountPercent;
    }
}
