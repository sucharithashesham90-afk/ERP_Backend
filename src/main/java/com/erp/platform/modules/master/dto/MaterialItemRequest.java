package com.erp.platform.modules.master.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MaterialItemRequest {
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
    private boolean active = true;
}
