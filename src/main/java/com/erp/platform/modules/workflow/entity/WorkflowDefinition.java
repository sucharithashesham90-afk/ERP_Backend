package com.erp.platform.modules.workflow.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_definitions",
       indexes = {@Index(name = "idx_wfdef_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WorkflowDefinition extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String module;

    @Column(length = 500)
    private String description;

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();
}
