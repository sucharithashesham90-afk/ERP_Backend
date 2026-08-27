package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CropGroupDto {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
