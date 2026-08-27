package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCropGroupRequest {

    @NotBlank(message = "Crop group name is required")
    private String name;

    private String description;
}
