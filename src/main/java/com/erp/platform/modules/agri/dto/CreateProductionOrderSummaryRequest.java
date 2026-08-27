package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;

public record CreateProductionOrderSummaryRequest(
        String orderNumber,
        String season,
        String cropName,
        String varietyName,
        BigDecimal plannedQtyKgs,
        BigDecimal actualQtyKgs,
        String location,
        String processingStatus,
        String remarks
) {}
