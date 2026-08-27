package com.erp.platform.modules.auth.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RoleDto {
    private UUID id;
    private String name;
    private String description;
    private boolean system;
    private UUID tenantId;
    private Set<String> allowedModules;
    private Set<String> allowedScreens;
}
