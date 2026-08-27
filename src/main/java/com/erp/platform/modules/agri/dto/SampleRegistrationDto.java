package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SampleRegistrationDto {

    private UUID id;
    private String sampleNumber;
    private String batchNumber;
    private String lotNumber;
    private String registrationNumber;
    private String cropGroupId;
    private String cropGroupName;
    private String cropId;
    private String cropName;
    private String varietyId;
    private String varietyName;
    private String seedStateId;
    private String seedStateName;
    private String cropVarietyTestId;
    private LocalDate sampleDate;
    private String testLocationId;
    private String testLocationName;
    private BigDecimal sampleWeightGrams;
    private String submittedBy;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
