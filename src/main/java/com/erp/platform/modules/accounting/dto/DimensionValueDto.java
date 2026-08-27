package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DimensionValueDto {
    private UUID id;
    private UUID dimensionId;
    private String dimensionCode;
    private String code;
    private String name;
    private String description;
    private UUID parentId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
