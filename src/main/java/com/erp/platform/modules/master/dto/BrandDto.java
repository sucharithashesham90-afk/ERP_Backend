package com.erp.platform.modules.master.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BrandDto {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String cropGroupId;
    private String cropGroupName;
    private String cropId;
    private String cropName;
    private String varietyId;
    private String varietyName;
    private String description;
    private String logoUrl;
    private String country;
    private String salesScope;
    private String salesAreas;
    private boolean useSticker;
    private String stickerMaterial;
    private String imageData;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
