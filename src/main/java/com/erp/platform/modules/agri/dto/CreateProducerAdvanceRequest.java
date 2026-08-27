package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProducerAdvanceRequest {
    @NotBlank
    private String advanceNumber;
    @NotNull
    private UUID fieldProducerId;
    private UUID producerContractId;
    @NotNull
    private LocalDate advanceDate;
    @NotNull
    private BigDecimal advanceAmount;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String remarks;
}
