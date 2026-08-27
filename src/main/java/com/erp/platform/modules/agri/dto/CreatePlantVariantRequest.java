package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePlantVariantRequest {

    @NotBlank
    private String name;
    private String aliasName;
    private String code;
    private boolean active = true;

    // Tab 1 — Basic / Classification
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropId;
    private String cropName;
    private UUID plantCategoryId;
    private UUID seedClassId;
    private String seedClassName;
    private UUID seedCategoryId;
    private String seedCategoryName;
    private Integer yearOfRelease;
    private String releaseYear;
    private String notificationNumber;
    private String division;

    // Tab 2 — Variety Details
    private boolean hybrid = false;
    private boolean transgenic = false;
    private boolean productWiseSalesPlan = false;
    private String gotRequired;
    private BigDecimal averageSeedWeight;
    private BigDecimal plantProductWeight;
    private BigDecimal maxProcessLoss;
    private BigDecimal minProcessLoss;
    private BigDecimal inWtPerMoisture;
    private Integer sowHarvestPrd;
    private BigDecimal isolationDist;
    private Integer maxOffTypesCount;
    private Integer maxSelfsCount;
    private String screens;
    private Integer expirationPeriodValue;
    private String expirationPeriodUnit;
    private String characteristics;
    private String treatments;

    // Tab 3 — Production & Processing
    private List<String> productionAreaIds;
    private String productionAreaNames;
    private List<String> processingPlantIds;
    private String processingPlantNames;

    // Legacy
    private String description;
    private String licensingAuthority;
    private BigDecimal standardViabilityRate;
    private BigDecimal standardQualityGrade;
    private BigDecimal standardMoistureLevel;
    private String productionSeason;
    private String maturityDays;
}
