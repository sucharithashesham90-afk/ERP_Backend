package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "period_definitions",
       indexes = {
           @Index(name = "idx_pd_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pd_code",   columnList = "tenant_id, period_code")
       })
@Getter
@Setter
public class PeriodDefinition extends TenantEntity {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "period_type", nullable = false, length = 30)
    private String periodType = "Yearly";

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode;

    @Column(name = "period_status", nullable = false, length = 30)
    private String periodStatus = "Initialized";
}
