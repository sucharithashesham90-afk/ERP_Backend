package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlantFamilyDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private String scientificName;
    private boolean active;
    private LocalDateTime createdAt;
}
