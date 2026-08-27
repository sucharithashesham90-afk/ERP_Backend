package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateSubAccountRequest {

    @NotBlank
    private String name;

    private String code;
    private String subAccountType;
    private String parentAccountCode;
    private String description;
    private UUID plantVariantId;
    private String plantVariantName;
    private boolean active = true;
}
