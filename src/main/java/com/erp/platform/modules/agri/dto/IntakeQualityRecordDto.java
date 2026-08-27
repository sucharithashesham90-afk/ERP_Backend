package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IntakeQualityRecordDto {
    private UUID id;
    private UUID intakeSlipId;
    private String intakeSlipNumber;
    private LocalDate recordDate;
    private UUID plantVariantId;
    private String plantVariantName;
    private UUID fieldProducerId;
    private String fieldProducerName;
    private BigDecimal grossWeight;
    private BigDecimal moistureDeductionPercent;
    private BigDecimal trashDeductionPercent;
    private BigDecimal netWeight;
    private BigDecimal initialViabilityRate;
    private BigDecimal initialQualityGrade;
    private String qualityStatus;
    private String remarks;
    private LocalDateTime createdAt;
}
