package com.erp.platform.modules.payroll.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salary_components",
       indexes = {@Index(name = "idx_salcomp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SalaryComponent extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 15)
    private String type; // EARNING, DEDUCTION

    @Column(name = "calculation_type")
    private String calculationType = "FIXED";

    private double value = 0;

    @Column(name = "calculation_base", length = 20)
    private String calculationBase; // BASIC, GROSS

    private boolean taxable = true;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
