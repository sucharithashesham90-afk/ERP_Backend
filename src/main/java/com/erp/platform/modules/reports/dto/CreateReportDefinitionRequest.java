package com.erp.platform.modules.reports.dto;

import com.erp.platform.modules.reports.entity.ReportDefinition.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReportDefinitionRequest {

    @NotBlank(message = "Report name is required")
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
    private boolean active = true;
    private String notes;
}
