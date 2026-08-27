package com.erp.platform.modules.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AccountDefaultDto {

    private UUID id;
    private String defaultKey;
    private String defaultValue;
    private String description;
    private String category;
    private String periodType;
    private boolean active;
    private LocalDateTime createdAt;
}
