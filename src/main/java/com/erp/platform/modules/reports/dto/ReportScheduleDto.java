package com.erp.platform.modules.reports.dto;

import com.erp.platform.modules.reports.entity.ReportSchedule.Frequency;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportScheduleDto {

    private UUID id;
    private UUID reportDefinitionId;
    private String scheduleName;
    private Frequency frequency;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private int hour;
    private String recipients;
    private String format;
    private boolean active;
    private LocalDateTime lastRunAt;
    private LocalDateTime nextRunAt;
    private LocalDateTime createdAt;
}
