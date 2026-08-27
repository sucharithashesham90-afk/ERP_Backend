package com.erp.platform.modules.reports.dto;

import com.erp.platform.modules.reports.entity.ReportDefinition.ReportCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportDefinitionDto {

    private UUID id;
    private String reportCode;
    private String name;
    private String description;
    private ReportCategory reportCategory;
    private String baseEntity;
    private String columns;
    private String defaultFilters;
    private String defaultSortField;
    private String defaultSortDirection;
    private int defaultPageSize;
    private String groupByField;
    private String aggregations;
    private boolean isPublic;
    private String createdBy;
    private LocalDateTime lastRunAt;
    private int runCount;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
