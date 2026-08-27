package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProducerPaymentRequest {
    @NotBlank
    private String paymentNumber;
    private LocalDate paymentDate;
    private UUID fieldProducerId;
    private UUID producerContractId;
    private BigDecimal deliveredQuantity;
    private BigDecimal qualifiedQuantity;
    private BigDecimal rejectedQuantity;
    private BigDecimal ratePerUnit;
    private BigDecimal grossAmount;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String remarks;
}
