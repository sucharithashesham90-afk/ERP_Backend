package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.UnitOfMeasure.UoMType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UnitOfMeasureDto {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String symbol;
    private UoMType uomType;
    private boolean baseUnit;
    private int decimalPlaces;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
