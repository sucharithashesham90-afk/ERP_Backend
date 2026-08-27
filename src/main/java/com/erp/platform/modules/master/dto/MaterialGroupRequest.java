package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class MaterialGroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String description;

    private UUID parentGroupId;
    private String parentGroupName;
    private boolean batchWiseReceipt;
    private boolean hasExpiryPeriod;
    private String expiryDate;
    private boolean sellable;
    private UUID uomCategoryId;
    private String uomCategoryName;

    private boolean active = true;
}
