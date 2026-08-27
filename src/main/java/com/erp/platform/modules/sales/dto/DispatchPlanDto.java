package com.erp.platform.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DispatchPlanDto(
        UUID id,
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
