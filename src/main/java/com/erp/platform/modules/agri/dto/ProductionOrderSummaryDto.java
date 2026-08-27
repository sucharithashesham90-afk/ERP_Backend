package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductionOrderSummaryDto(
        UUID id,
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
