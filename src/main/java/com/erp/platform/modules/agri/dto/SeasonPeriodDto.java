package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeasonPeriodDto {
    private UUID id;
    private UUID seasonId;
    private String seasonName;
    private String periodName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String description;
    private boolean active;
    private String periodType;
    private LocalDateTime createdAt;
}
