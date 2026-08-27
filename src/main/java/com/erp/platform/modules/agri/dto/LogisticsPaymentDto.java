package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LogisticsPaymentDto {
    private UUID id;
    private String paymentNumber;
    private LocalDate paymentDate;
    private UUID fieldProducerId;
    private String fieldProducerName;
    private UUID producerContractId;
    private String contractNumber;
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
    private LocalDateTime createdAt;
}
