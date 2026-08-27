package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlantCategoryDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private UUID plantFamilyId;
    private String plantFamilyName;
    private String scientificName;
    private String defaultSeason;
    private boolean active;
    private LocalDateTime createdAt;
}
