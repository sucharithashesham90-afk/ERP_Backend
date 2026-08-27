package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateLogisticsPaymentRequest {
    @NotBlank
    private String paymentNumber;
    private LocalDate paymentDate;
    private UUID fieldProducerId;
    private UUID producerContractId;
    private String logisticsProvider;
    private String vehicleNumber;
    private BigDecimal quantityHandled;
    private String handlingUom;
    private BigDecimal ratePerUnit;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String remarks;
}
