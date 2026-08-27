package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Net Sales Closing (per the Sales module spec): closing the net sales of a selected season period.
 * Once closed, that period no longer appears in the next season's net sales.
 */
@Entity
@Table(name = "net_sales_closings",
       indexes = {
           @Index(name = "idx_nsc_tenant", columnList = "tenant_id"),
           @Index(name = "idx_nsc_period", columnList = "tenant_id, season_period_id")
       })
@Getter
@Setter
public class NetSalesClosing extends TenantEntity {

    @Column(name = "season_period_id", nullable = false)
    private UUID seasonPeriodId;

    @Column(name = "season_period_name", length = 200)
    private String seasonPeriodName;

    @Column(nullable = false)
    private boolean closed = true;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 150)
    private String closedBy;
}
