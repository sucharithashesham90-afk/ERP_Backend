package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RoleHierarchyDto {
    private UUID id;
    private UUID tenantId;
    private String roleName;
    private String displayName;
    private String parentRole;
    private String parentDisplayName;
    private int levelOrder;
    private String description;
    private LocalDateTime createdAt;
}
