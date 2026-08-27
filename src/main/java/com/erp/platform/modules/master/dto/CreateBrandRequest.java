package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBrandRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    // Crop hierarchy — a brand is saved against exactly one variety (1:1).
    private String cropGroupId;
    private String cropGroupName;
    private String cropId;
    private String cropName;
    private String varietyId;
    private String varietyName;

    private String description;

    private String logoUrl;

    private String country;

    private String salesScope = "ALL";
    private String salesAreas;
    private boolean useSticker = false;
    private String stickerMaterial;
    private String imageData;

    private boolean active = true;

    private String notes;
}
