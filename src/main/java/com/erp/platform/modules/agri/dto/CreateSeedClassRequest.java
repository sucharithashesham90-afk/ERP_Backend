package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSeedClassRequest {

    private String code;

    @NotBlank
    private String name;

    private String description;
    private String minimumPurity;
    private String minimumGermination;
    private boolean active = true;
}
