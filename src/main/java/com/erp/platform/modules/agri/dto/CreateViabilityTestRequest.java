package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateViabilityTestRequest {
    @NotBlank
    private String testNumber;
    private String lotNumber;
    private UUID plantVariantId;
    @NotNull
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
}
