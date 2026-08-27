package com.erp.platform.modules.reports.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_schedules",
       indexes = {@Index(name = "idx_rpt_sched_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ReportSchedule extends TenantEntity {

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_definition_id", nullable = false)
    private ReportDefinition reportDefinition;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Frequency frequency;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "hour")
    private int hour = 8;

    @Column(length = 1000)
    private String recipients;

    @Column(length = 20)
    private String format = "CSV";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;
}
