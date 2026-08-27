package com.erp.platform.modules.reports.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_definitions",
       indexes = {@Index(name = "idx_rpt_def_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ReportDefinition extends TenantEntity {

    public enum ReportCategory {
        SALES, PURCHASE, INVENTORY, FINANCIAL, HR, MANUFACTURING, QUALITY, CUSTOM
    }

    @Column(name = "report_code", nullable = false, length = 50)
    private String reportCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_category", length = 30)
    private ReportCategory reportCategory;

    @Column(name = "base_entity", length = 100)
    private String baseEntity;

    @Column(columnDefinition = "TEXT")
    private String columns;

    @Column(name = "default_filters", columnDefinition = "TEXT")
    private String defaultFilters;

    @Column(name = "default_sort_field", length = 100)
    private String defaultSortField;

    @Column(name = "default_sort_direction", length = 10)
    private String defaultSortDirection = "DESC";

    @Column(name = "default_page_size")
    private int defaultPageSize = 20;

    @Column(name = "group_by_field", length = 100)
    private String groupByField;

    @Column(columnDefinition = "TEXT")
    private String aggregations;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "run_count")
    private int runCount = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
