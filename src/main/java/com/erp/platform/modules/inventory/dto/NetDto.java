package com.erp.platform.modules.inventory.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** Request + response DTO for the Nets screen. */
@Data
public class NetDto {
    private UUID id;
    private String name;
    private String location;
    private UUID godownId;
    private String godownName;
    private String landmark;
    private String dimension;
    private String positionInGodown;
    private boolean active = true;
    private LocalDateTime createdAt;
}
