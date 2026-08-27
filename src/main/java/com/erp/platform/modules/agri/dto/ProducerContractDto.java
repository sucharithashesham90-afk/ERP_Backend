package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProducerContractDto {
    private UUID id;
    private String contractNumber;
    private UUID fieldProducerId;
    private String fieldProducerName;
    private UUID plantVariantId;
    private String plantVariantName;
    private LocalDate contractDate;
    private LocalDate expectedHarvestDate;
    private BigDecimal contractedArea;
    private BigDecimal expectedQuantity;
    private BigDecimal agreedRate;
    private String status;
    private String season;
    private String termsAndConditions;
    private String remarks;
    private LocalDateTime createdAt;
}
