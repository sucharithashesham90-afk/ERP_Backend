package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductLineDto {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String description;
    private UUID brandId;
    private String brandName;
    private String category;
    private String targetMarket;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
