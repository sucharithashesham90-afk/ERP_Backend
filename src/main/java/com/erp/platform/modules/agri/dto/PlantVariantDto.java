package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PlantVariantDto {
    private UUID id;
    private String name;
    private String aliasName;
    private String code;
    private boolean active;

    // Tab 1 — Basic / Classification
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropId;
    private String cropName;
    private UUID plantCategoryId;
    private String plantCategoryName;
    private UUID seedClassId;
    private String seedClassName;
    private UUID seedCategoryId;
    private String seedCategoryName;
    private Integer yearOfRelease;
    private String releaseYear;
    private String notificationNumber;
    private String division;

    // Tab 2 — Variety Details
    private boolean hybrid;
    private boolean transgenic;
    private boolean productWiseSalesPlan;
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

    private LocalDateTime createdAt;
}
