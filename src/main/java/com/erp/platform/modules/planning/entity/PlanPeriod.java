package com.erp.platform.modules.planning.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "plan_periods", indexes = {@Index(name = "idx_plan_period_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PlanPeriod extends TenantEntity {

    @Column(length = 200)
    private String periodName;

    private LocalDate fromDate;

    private LocalDate toDate;

    @Column(length = 100)
    private String planType;

    @Column(length = 50)
    private String status = "OPEN";
}
