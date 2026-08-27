package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaterialGroupDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String description;
    private UUID parentGroupId;
    private String parentGroupName;
    private boolean batchWiseReceipt;
    private boolean hasExpiryPeriod;
    private String expiryDate;
    private boolean sellable;
    private UUID uomCategoryId;
    private String uomCategoryName;
    private boolean active;
    private LocalDateTime createdAt;
}
