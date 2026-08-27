package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProducerAdvanceDto {
    private UUID id;
    private String advanceNumber;
    private UUID fieldProducerId;
    private String fieldProducerName;
    private UUID producerContractId;
    private String contractNumber;
    private LocalDate advanceDate;
    private BigDecimal advanceAmount;
    private BigDecimal adjustedAmount;
    private String paymentMethod;
    private String referenceNumber;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
