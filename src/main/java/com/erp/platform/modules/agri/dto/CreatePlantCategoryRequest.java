package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreatePlantCategoryRequest {
    @NotBlank
    private String name;
    private String code;
    private String description;
    private UUID plantFamilyId;
    private String scientificName;
    private String defaultSeason;
}
