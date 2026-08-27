package com.erp.platform.modules.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LocationDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}
