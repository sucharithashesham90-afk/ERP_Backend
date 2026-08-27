package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "possible_deductions", indexes = {@Index(name = "idx_possible_deduction_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PossibleDeduction extends TenantEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "deduction_type", length = 50)
    private String deductionType;

    @Column(name = "is_per_bag")
    private boolean isPerBag;

    @Column(name = "is_weight_deduction")
    private boolean isWeightDeduction;

    @Column(name = "is_cost_deduction")
    private boolean isCostDeduction;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "units", length = 50)
    private String units;

    @Column(name = "active")
    private boolean active = true;
}
