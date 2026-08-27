package com.erp.platform.modules.master.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaterialItemDto {
    private UUID id;
    private UUID tenantId;
    private UUID materialId;
    private String materialName;
    private UUID materialGroupId;
    private String materialGroupName;
    private String name;
    private String code;
    private String description;
    private String unit;
    private boolean batchWiseReceipt;
    private boolean hasExpiryPeriod;
    private String expiryDate;
    private boolean sellable;
    private boolean active;
    private LocalDateTime createdAt;
}
