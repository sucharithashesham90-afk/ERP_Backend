package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "fiscal_years",
       indexes = {
           @Index(name = "idx_fy_tenant", columnList = "tenant_id"),
           @Index(name = "idx_fy_code",   columnList = "tenant_id, period_code", unique = true)
       })
@Getter
@Setter
public class FiscalYear extends TenantEntity {

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode; // e.g. "2024-25"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "period_type", length = 20)
    private String periodType = "YEARLY"; // YEARLY, QUARTERLY, MONTHLY

    @Column(length = 20)
    private String status = "INITIALIZED"; // INITIALIZED, CLOSED

    @Column(length = 500)
    private String description;
}
