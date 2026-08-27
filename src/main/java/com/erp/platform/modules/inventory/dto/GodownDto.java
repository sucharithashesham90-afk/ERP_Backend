package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Request + response DTO for the Godowns screen. */
@Data
public class GodownDto {
    private UUID id;
    private String name;
    private String location;
    private boolean machineAvailability;
    private List<String> groups;
    private String area;
    private boolean coldStorage;
    private boolean packingMaterialStorage;
    private String ownership;
    private String storageCapacity;
    private String storageCapacityUom;
    private boolean active = true;
    private LocalDateTime createdAt;
}
