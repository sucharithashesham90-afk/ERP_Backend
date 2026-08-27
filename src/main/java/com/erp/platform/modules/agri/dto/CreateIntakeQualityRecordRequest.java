package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateIntakeQualityRecordRequest {
    private UUID intakeSlipId;
    private String intakeSlipNumber;
    private LocalDate recordDate;
    private UUID plantVariantId;
    private UUID fieldProducerId;
    private BigDecimal grossWeight;
    private BigDecimal moistureDeductionPercent;
    private BigDecimal trashDeductionPercent;
    private BigDecimal netWeight;
    private BigDecimal initialViabilityRate;
    private BigDecimal initialQualityGrade;
    private String qualityStatus;
    private String remarks;
}
