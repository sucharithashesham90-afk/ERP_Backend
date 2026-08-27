package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductCategoryDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String description;
    private UUID parentId;
    private String parentName;
    private boolean active;
    private LocalDateTime createdAt;
}
