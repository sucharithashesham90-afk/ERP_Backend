package com.erp.platform.modules.accounting.dto;

import com.erp.platform.config.LenientUUIDDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateBudgetEntryRequest {

    @NotNull
    private Integer periodYear;

    @NotNull
    private Integer periodMonth;

    @JsonDeserialize(using = LenientUUIDDeserializer.class)
    private UUID accountId;

    private String accountCode;

    private String accountName;

    @JsonDeserialize(using = LenientUUIDDeserializer.class)
    private UUID costCenterId;

    @JsonDeserialize(using = LenientUUIDDeserializer.class)
    private UUID dimensionValueId;

    @NotNull
    private BigDecimal budgetAmount;

    private BigDecimal actualAmount;

    private String notes;
}
