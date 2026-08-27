package com.erp.platform.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDispatchPlanRequest(
        String planNumber,
        LocalDate planDate,
        String salesOrderNumber,
        String customerName,
        String location,
        String productName,
        BigDecimal plannedQtyKgs,
        String vehicleNumber,
        LocalDate plannedDispatchDate,
        String status
) {}
