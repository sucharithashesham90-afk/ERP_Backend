package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TestLocationDto {

    private UUID id;
    private String name;
    private String description;
    private String city;
    private String state;
    private boolean active;
    private LocalDateTime createdAt;
}
