package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.StorageLocation.LocationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StorageLocationDto {

    private UUID id;
    private UUID tenantId;
    private UUID warehouseId;
    private String warehouseName;
    private String code;
    private String name;
    private LocationType locationType;
    private String aisle;
    private String rack;
    private String bin;
    private BigDecimal capacity;
    private String capacityUnit;
    private BigDecimal currentOccupancy;
    private boolean active;
    private boolean isDefault;
    private String notes;
    private LocalDateTime createdAt;
}
