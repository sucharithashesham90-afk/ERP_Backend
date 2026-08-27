package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateCropVarietyTestRequest {

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
    private boolean updateInventory = false;
    private boolean mandatory = false;
    private String processSeedStateIds;
    private boolean defined = false;
    private boolean active = true;

    private List<LocationConfigRequest> locationConfigs;
    private List<PropertyStandardRequest> propertyStandards;

    @Getter
    @Setter
    public static class LocationConfigRequest {
        private String testLocationId;
        private String testLocationName;
        private BigDecimal testCost;
        private String testDuration;
    }

    @Getter
    @Setter
    public static class PropertyStandardRequest {
        private String propertyName;
        private String testLocationId;
        private String testLocationName;
        private BigDecimal minValue;
        private BigDecimal maxValue;
    }
}
