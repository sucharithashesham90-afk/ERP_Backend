package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UoMConversionDto {
    private UUID id;
    private UUID tenantId;
    private UUID fromUomId;
    private String fromUomCode;
    private UUID toUomId;
    private String toUomCode;
    private BigDecimal conversionFactor;
    private boolean bidirectional;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
