package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BudgetEntryDto {
    private UUID id;
    private int periodYear;
    private int periodMonth;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private UUID costCenterId;
    private UUID dimensionValueId;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal variance;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
