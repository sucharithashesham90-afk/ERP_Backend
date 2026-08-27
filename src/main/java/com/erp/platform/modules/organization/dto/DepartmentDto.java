package com.erp.platform.modules.organization.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DepartmentDto {
    private UUID id;
    private UUID tenantId;
    private UUID companyId;
    private String name;
    private String code;
    private String description;
    private UUID parentId;
    private boolean active;
    private LocalDateTime createdAt;
}
