package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DealerRegionDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private String statesCovered;
    private String activeSeason;
    private boolean active;
    private LocalDateTime createdAt;
}
