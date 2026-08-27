package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeedStateDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
}
