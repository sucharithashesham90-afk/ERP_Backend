package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleHierarchyRequest {
    @NotBlank(message = "Role name is required")
    @Size(max = 100)
    private String roleName;

    @Size(max = 150)
    private String displayName;

    @Size(max = 100)
    private String parentRole;

    @Min(1)
    private int levelOrder = 99;

    @Size(max = 500)
    private String description;
}
