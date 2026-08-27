package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.UnitOfMeasure.UoMType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUoMRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String symbol;

    @NotNull
    private UoMType uomType;

    private boolean baseUnit = false;

    private int decimalPlaces = 2;

    private boolean active = true;

    private String notes;
}
