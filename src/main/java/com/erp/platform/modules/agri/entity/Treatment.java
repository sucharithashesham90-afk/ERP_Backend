package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "treatments",
        indexes = {@Index(name = "idx_treatment_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Treatment extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String chemicalName;

    @Column(length = 100)
    private String treatmentType;

    @Column(precision = 10, scale = 4)
    private BigDecimal dosagePerKg;

    @Column(length = 50)
    private String unit;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
