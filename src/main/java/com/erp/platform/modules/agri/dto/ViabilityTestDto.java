package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ViabilityTestDto {
    private UUID id;
    private String testNumber;
    private String lotNumber;
    private UUID plantVariantId;
    private String plantVariantName;
    private LocalDate testDate;
    private LocalDate resultDate;
    private String testLab;
    private String testedBy;
    private BigDecimal viabilityRate;
    private BigDecimal qualityGrade;
    private BigDecimal moistureContent;
    private BigDecimal inertMatter;
    private BigDecimal otherSeeds;
    private String testResult;
    private String status;
    private BigDecimal sampleSize;
    private String remarks;
    private LocalDateTime createdAt;
}
