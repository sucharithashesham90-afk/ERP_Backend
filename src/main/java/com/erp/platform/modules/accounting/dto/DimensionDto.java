package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DimensionDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private boolean mandatory;
    private boolean active;
    private List<DimensionValueDto> values;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
