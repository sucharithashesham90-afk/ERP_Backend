package com.erp.platform.modules.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerDeliveryOrderDto(
        UUID id,
        String doNumber,
        LocalDate doDate,
        String salesOrderNumber,
        String customerName,
        String lorryNumber,
        String carrier,
        BigDecimal freightToPay,
        String deliveredFrom,
        String status
) {}
