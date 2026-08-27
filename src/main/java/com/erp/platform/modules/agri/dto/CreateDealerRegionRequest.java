package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDealerRegionRequest {
    @NotBlank
    private String name;
    private String code;
    private String description;
    private String statesCovered;
    private String activeSeason;
    private boolean active = true;
}
