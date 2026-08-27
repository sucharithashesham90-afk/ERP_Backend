package com.erp.platform.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLocationRequest {

    @NotBlank(message = "Location name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String description;

    private boolean active = true;
}
