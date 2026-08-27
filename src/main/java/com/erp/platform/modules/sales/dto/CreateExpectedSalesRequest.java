package com.erp.platform.modules.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Every field is mandatory except expectedDealerBalanceKgs and remarks. */
@Getter
@Setter
public class CreateExpectedSalesRequest {

    @NotBlank(message = "Crop group is required")
    private String cropGroup;

    @NotBlank(message = "Crop is required")
    private String cropName;

    @NotBlank(message = "Variety is required")
    private String varietyName;

    @NotBlank(message = "Sales area is required")
    private String salesArea;

    @NotBlank(message = "Sales period is required")
    private String salesPeriod;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @NotNull(message = "Expected sales (Kg) is required")
    private BigDecimal expectedSalesKgs;

    private BigDecimal expectedDealerBalanceKgs;
    private String remarks;
}
