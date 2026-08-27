package com.erp.platform.modules.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payslip_lines")
@Getter
@Setter
public class PayslipLine extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id", nullable = false)
    @JsonIgnore
    private Payslip payslip;

    @Column(name = "component_id")
    private UUID componentId;

    @Column(name = "component_name", length = 100)
    private String componentName;

    @Column(name = "component_type", length = 15)
    private String componentType; // EARNING, DEDUCTION

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
}
