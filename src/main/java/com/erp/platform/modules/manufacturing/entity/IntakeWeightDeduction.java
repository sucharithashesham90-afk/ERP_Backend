package com.erp.platform.modules.manufacturing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "intake_weight_deductions",
        indexes = {
                @Index(name = "idx_iwd_intake", columnList = "intake_id")
        })
@Getter
@Setter
public class IntakeWeightDeduction extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id", nullable = false)
    @JsonIgnore
    private ProductionIntake intake;

    @Column(name = "deduction_item", nullable = false, length = 200)
    private String deductionItem;

    @Column(name = "quantity_kg", precision = 10, scale = 3)
    private BigDecimal quantityKg = BigDecimal.ZERO;
}
