package com.erp.platform.modules.auth.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserGroupDto {
    private UUID id;
    private String name;
    private String description;
    private String groupLeader;
    private String groupEmail;
    private Integer hierarchyLevel;
    private List<String> allowedRoles;
    private long memberCount;
}
