package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SubAccountDto {

    private UUID id;
    private String name;
    private String code;
    private String subAccountType;
    private String parentAccountCode;
    private String description;
    private UUID plantVariantId;
    private String plantVariantName;
    private boolean active;
    private LocalDateTime createdAt;
}
