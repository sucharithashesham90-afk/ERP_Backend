package com.erp.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateServerConfigRequest {

    @NotBlank
    private String configKey;

    private String configValue;
    private String description;
    private String category;
    private boolean active = true;
}
