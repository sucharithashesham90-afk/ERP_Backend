package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateUoMConversionRequest {

    @NotNull
    private UUID fromUomId;

    @NotNull
    private UUID toUomId;

    @NotNull
    @DecimalMin(value = "0.00000001", message = "Conversion factor must be positive")
    private BigDecimal conversionFactor;

    private boolean bidirectional = true;

    private String notes;
}
