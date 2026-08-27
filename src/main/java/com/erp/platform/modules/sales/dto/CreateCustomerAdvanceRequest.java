package com.erp.platform.modules.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateCustomerAdvanceRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate paymentDate;
    private String paymentMode;
    private String referenceNumber;
    private String notes;
}
