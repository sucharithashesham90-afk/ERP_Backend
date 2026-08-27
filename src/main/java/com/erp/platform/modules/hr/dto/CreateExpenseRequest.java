package com.erp.platform.modules.hr.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateExpenseRequest {

    @NotNull
    private UUID employeeId;

    @NotNull
    private LocalDate expenseDate;

    private String category;
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String currency;
    private String receiptReference;
    private String notes;
}
