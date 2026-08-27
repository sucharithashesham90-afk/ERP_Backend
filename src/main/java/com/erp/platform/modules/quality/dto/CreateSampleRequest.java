package com.erp.platform.modules.quality.dto;

import com.erp.platform.modules.quality.entity.Sample.SourceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSampleRequest {

    private UUID productId;
    private String productName;
    private SourceType sourceType;
    private UUID sourceId;
    private String sourceReference;
    private String lotNumber;
    private LocalDate sampleDate;
    private BigDecimal sampleSize;
    private String unit;
    private String collectedBy;
    private UUID warehouseId;
    private String cropGroupId;
    private String cropGroupName;
    private String cropId;
    private String cropName;
    private String varietyId;
    private String varietyName;
    private String seedStateId;
    private String seedStateName;
    private String location;
    private UUID cropVarietyTestId;
    private String notes;
}
