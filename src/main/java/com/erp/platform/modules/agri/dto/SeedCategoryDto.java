package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeedCategoryDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}
