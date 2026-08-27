package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "work_centers",
       indexes = {@Index(name = "idx_wc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WorkCenter extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String type; // MACHINE, LABOR, OUTSOURCE, ASSEMBLY

    @Column(length = 100)
    private String location;

    @Column(precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit = "HOURS/DAY";

    @Column(name = "cost_per_hour", precision = 10, scale = 2)
    private BigDecimal costPerHour;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String notes;
}
