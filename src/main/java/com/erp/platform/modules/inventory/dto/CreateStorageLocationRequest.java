package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.StorageLocation.LocationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateStorageLocationRequest {

    private UUID warehouseId;
    private String warehouseName;
    private String code;
    @NotBlank(message = "Name is required")
    private String name;
    private LocationType locationType;
    private String aisle;
    private String rack;
    private String bin;
    private BigDecimal capacity;
    private String capacityUnit;
    private Boolean active;
    private Boolean isDefault;
    private String notes;
}
