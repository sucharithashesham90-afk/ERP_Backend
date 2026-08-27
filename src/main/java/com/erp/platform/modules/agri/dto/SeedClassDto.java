package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeedClassDto {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String minimumPurity;
    private String minimumGermination;
    private boolean active;
    private LocalDateTime createdAt;
}
