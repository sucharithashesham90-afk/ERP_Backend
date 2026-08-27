package com.erp.platform.modules.promotions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PromotionRedemptionDto {

    private UUID id;
    private UUID tenantId;
    private UUID promotionId;
    private String promotionCode;
    private String promotionName;
    private UUID salesOrderId;
    private UUID customerId;
    private String customerName;
    private LocalDate redemptionDate;
    private BigDecimal discountApplied;
    private String notes;
    private LocalDateTime createdAt;
}
