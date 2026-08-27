package com.erp.platform.modules.reports.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_runs",
       indexes = {@Index(name = "idx_rpt_run_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ReportRun extends TenantEntity {

    public enum RunStatus {
        SUCCESS, FAILED, RUNNING
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_definition_id", nullable = false)
    private ReportDefinition reportDefinition;

    @Column(name = "run_by", length = 100)
    private String runBy;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(columnDefinition = "TEXT")
    private String filters;

    @Column(name = "record_count")
    private int recordCount;

    @Column(name = "duration_ms")
    private long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RunStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
