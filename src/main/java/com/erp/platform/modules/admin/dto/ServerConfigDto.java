package com.erp.platform.modules.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ServerConfigDto {

    private UUID id;
    private String configKey;
    private String configValue;
    private String description;
    private String category;
    private boolean active;
    private LocalDateTime createdAt;
}
