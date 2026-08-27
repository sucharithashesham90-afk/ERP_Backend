package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProcessingSequenceDto {

    private UUID id;
    private String name;
    private String processingSteps;
    private String stepsJson;
    private boolean active;
    private LocalDateTime createdAt;
}
