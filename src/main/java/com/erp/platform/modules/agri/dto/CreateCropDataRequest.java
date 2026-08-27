package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateCropDataRequest(
        @NotBlank String cropName,
        String cropCode,
        UUID cropGroupId,
        String cropGroupName,
        List<String> seasonIds,
        String seasonNames,
        Integer noOfInspections,
        String uom,
        // Automatic Sample Creation
        boolean sampleAtIntake,
        boolean sampleAtSalesReturn,
        boolean sampleAtThirdParty,
        boolean sampleAtSeedExpire,
        // Timing & Sizing
        BigDecimal certificationTime,
        String certificationTimeUnit,
        BigDecimal processingTime,
        String processingTimeUnit,
        BigDecimal lotSizeQty,
        BigDecimal isolationDistance,
        // Parent Seed Pack
        String parentSeedPackType,
        BigDecimal parentSeedPackSizeKgs,
        // Material
        String materialState,
        String productName,
        String materialType,
        // General
        String notes
) {}
