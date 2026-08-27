package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DimensionMappingDto {
    private UUID id;
    private String referenceType;
    private UUID referenceId;
    private UUID dimensionId;
    private String dimensionCode;
    private String dimensionName;
    private UUID dimensionValueId;
    private String dimensionValueCode;
    private String dimensionValueName;
    private BigDecimal amount;
    private String notes;
    private LocalDateTime createdAt;
}
