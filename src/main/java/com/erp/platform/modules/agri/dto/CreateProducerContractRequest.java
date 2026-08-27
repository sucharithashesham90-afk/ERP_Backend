package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProducerContractRequest {
    @NotBlank
    private String contractNumber;
    @NotNull
    private UUID fieldProducerId;
    private UUID plantVariantId;
    @NotNull
    private LocalDate contractDate;
    private LocalDate expectedHarvestDate;
    private BigDecimal contractedArea;
    private BigDecimal expectedQuantity;
    private BigDecimal agreedRate;
    private String status;
    private String season;
    private String termsAndConditions;
    private String remarks;
}
