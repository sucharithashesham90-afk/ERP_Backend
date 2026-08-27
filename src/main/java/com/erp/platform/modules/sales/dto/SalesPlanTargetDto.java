package com.erp.platform.modules.sales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SalesPlanTargetDto {
    private UUID id;
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropId;
    private String cropName;
    private UUID varietyId;
    private String varietyName;
    private UUID productId;
    private String productName;
    private BigDecimal targetQuantity;
    private BigDecimal targetRevenue;
    private BigDecimal actualQuantity;
    private BigDecimal actualRevenue;
    private String unit;
}
