package com.erp.platform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    private String name;
    private String description;
    private Set<String> allowedModules;
    private Set<String> allowedScreens;
}
