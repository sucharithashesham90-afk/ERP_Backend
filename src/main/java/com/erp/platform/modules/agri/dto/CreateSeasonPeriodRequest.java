package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateSeasonPeriodRequest {

    @NotBlank
    private String periodName;

    private UUID seasonId;
    private String seasonName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String description;
    private boolean active = true;
    private String periodType;
}
