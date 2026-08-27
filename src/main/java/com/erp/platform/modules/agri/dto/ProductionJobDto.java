package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductionJobDto {
    private UUID id;
    private String jobNumber;
    private UUID planId;
    private String planNumber;
    private String planName;
    private UUID cropGroupId;
    private String cropGroupName;
    private UUID cropId;
    private String cropName;
    private UUID varietyId;
    private String varietyName;
    private UUID productionAreaId;
    private String productionAreaName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private String notes;
    private List<AgriJobAllocationDto> allocations;
    private LocalDateTime createdAt;
}
