package com.erp.platform.modules.workflow.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "approval_rules",
       indexes = {@Index(name = "idx_appr_rule_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ApprovalRule extends TenantEntity {

    public enum Operator {
        GREATER_THAN, LESS_THAN, EQUALS, BETWEEN, IN
    }

    public enum ApproverType {
        SPECIFIC_USER, ROLE, DEPARTMENT_HEAD, MANAGER, CEO, ANY
    }

    @Column(name = "workflow_definition_id")
    private UUID workflowDefinitionId;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "condition_field", length = 100)
    private String conditionField;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator", length = 20)
    private Operator conditionOperator;

    @Column(name = "condition_value", length = 500)
    private String conditionValue;

    @Column(name = "condition_value2", length = 500)
    private String conditionValue2;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", length = 30)
    private ApproverType approverType;

    @Column(name = "approver_value", length = 200)
    private String approverValue;

    @Column(name = "approval_level")
    private int approvalLevel;

    @Column(name = "sla_days")
    private int slaDays = 3;

    @Column(name = "escalate_to", length = 200)
    private String escalateTo;

    @Column(nullable = false)
    private boolean active = true;
}
