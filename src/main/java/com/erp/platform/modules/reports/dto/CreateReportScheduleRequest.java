package com.erp.platform.modules.reports.dto;

import com.erp.platform.modules.reports.entity.ReportSchedule.Frequency;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateReportScheduleRequest {

    private String scheduleName;
    private Frequency frequency;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private int hour;
    private String recipients;
    private String format;
    private boolean active = true;
    private LocalDateTime nextRunAt;
}
