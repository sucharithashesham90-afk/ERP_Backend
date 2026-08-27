package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlantFamilyRequest {
    @NotBlank
    private String name;
    private String code;
    private String description;
    private String scientificName;
}
