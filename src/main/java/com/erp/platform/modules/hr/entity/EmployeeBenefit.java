package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_benefits",
       indexes = {@Index(name = "idx_empben_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class EmployeeBenefit extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 30)
    private String type; // MONETARY, NON_MONETARY, INSURANCE, ALLOWANCE

    @Column(length = 500)
    private String description;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "is_percentage")
    private boolean percentage = false;

    @Column(name = "percentage_of")
    private String percentageOf; // BASIC, CTC, GROSS

    @Column(name = "taxable")
    private boolean taxable = false;

    @Column(nullable = false)
    private boolean active = true;
}
