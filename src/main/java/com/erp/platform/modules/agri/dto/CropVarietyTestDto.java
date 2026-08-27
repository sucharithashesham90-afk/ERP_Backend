package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CropVarietyTestDto {

    private UUID id;
    private String cropGroupId;
    private String cropGroupName;
    private String cropId;
    private String cropName;
    private String varietyId;
    private String varietyName;
    private String seedStateIds;
    private String testIds;
    private String testNames;
    private BigDecimal sampleQuantity;
    private String sampleQuantityUom;
    private boolean updateInventory;
    private boolean mandatory;
    private String processSeedStateIds;
    private boolean defined;
    private boolean active;
    private LocalDateTime createdAt;

    private List<LocationConfigDto> locationConfigs;
    private List<PropertyStandardDto> propertyStandards;

    @Getter
    @Setter
    public static class LocationConfigDto {
        private UUID id;
        private String testLocationId;
        private String testLocationName;
        private BigDecimal testCost;
        private String testDuration;
    }

    @Getter
    @Setter
    public static class PropertyStandardDto {
        private UUID id;
        private String propertyName;
        private String testLocationId;
        private String testLocationName;
        private BigDecimal minValue;
        private BigDecimal maxValue;
    }
}
