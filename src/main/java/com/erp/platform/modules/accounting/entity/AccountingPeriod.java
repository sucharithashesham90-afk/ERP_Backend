package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_periods",
       indexes = {
           @Index(name = "idx_ap_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ap_period", columnList = "tenant_id, period_year, period_month", unique = true)
       })
@Getter
@Setter
public class AccountingPeriod extends TenantEntity {

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(nullable = false, length = 50)
    private String name; // e.g. "April 2026"

    @Column(length = 15)
    @Enumerated(EnumType.STRING)
    private PeriodStatus status = PeriodStatus.OPEN;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 200)
    private String closedBy;

    public enum PeriodStatus {
        OPEN, CLOSED
    }
}
