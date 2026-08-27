package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateByproductTypeRequest {

    @NotBlank
    private String name;

    private String description;
    private boolean active = true;
}
