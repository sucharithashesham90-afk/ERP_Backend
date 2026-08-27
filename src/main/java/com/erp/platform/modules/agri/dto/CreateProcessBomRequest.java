package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProcessBomRequest {

    @NotBlank
    private String processingType;

    private String itemCode;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private String bomType;
    private boolean active = true;
}
