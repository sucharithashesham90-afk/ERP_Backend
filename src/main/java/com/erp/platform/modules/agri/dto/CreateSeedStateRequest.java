package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSeedStateRequest {

    private String code;

    @NotBlank
    private String name;

    private String description;
    private Integer sortOrder;
    private boolean active = true;
}
