package com.erp.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountDefaultRequest {

    @NotBlank
    private String defaultKey;

    private String defaultValue;
    private String description;
    private String category;
    private String periodType;
    private boolean active = true;
}
