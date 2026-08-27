package com.erp.platform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;
    private String description;
    private String groupLeader;
    private String groupEmail;
    private Integer hierarchyLevel;
    private List<String> allowedRoles;
}
