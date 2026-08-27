package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "season_periods", indexes = {@Index(name = "idx_season_period_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SeasonPeriod extends TenantEntity {

    @Column(name = "season_name", length = 100)
    private String seasonName;

    @Column(name = "period_name", length = 100)
    private String periodName;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "year")
    private Integer year;

    @Column(name = "season_id")
    private java.util.UUID seasonId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active")
    private boolean active = true;

    /** SALES (Sales Period Setup screen) or PRODUCTION (Season Periods, Production Configuration).
     *  Both screens used to write into this same table with no way to tell rows apart — this keeps
     *  them from showing each other's records while still sharing the storage. Nullable so existing
     *  rows (created before this column existed) don't need a migration; see the DataInitializer
     *  backfill for how they're classified after the fact. */
    @Column(name = "period_type", length = 20)
    private String periodType;
}
