package com.erp.platform.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StockReconciliationDto(
        UUID id,
        String reconNumber,
        LocalDate reconDate,
        String location,
        String materialCode,
        String materialName,
        BigDecimal bookQty,
        BigDecimal physicalQty,
        BigDecimal difference,
        String unit,
        String approvedBy,
        String remarks,
        String status
) {}
