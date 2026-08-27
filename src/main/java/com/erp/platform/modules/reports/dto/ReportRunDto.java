package com.erp.platform.modules.reports.dto;

import com.erp.platform.modules.reports.entity.ReportRun.RunStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportRunDto {

    private UUID id;
    private UUID reportDefinitionId;
    private String runBy;
    private LocalDateTime runAt;
    private String filters;
    private int recordCount;
    private long durationMs;
    private RunStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
