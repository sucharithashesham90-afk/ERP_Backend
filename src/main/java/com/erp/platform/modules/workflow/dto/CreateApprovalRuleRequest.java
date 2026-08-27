package com.erp.platform.modules.workflow.dto;

import com.erp.platform.modules.workflow.entity.ApprovalRule.ApproverType;
import com.erp.platform.modules.workflow.entity.ApprovalRule.Operator;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateApprovalRuleRequest {

    private UUID workflowDefinitionId;
    private String ruleName;
    private String documentType;
    private String conditionField;
    private Operator conditionOperator;
    private String conditionValue;
    private String conditionValue2;
    private ApproverType approverType;
    private String approverValue;
    private int approvalLevel;
    private int slaDays;
    private String escalateTo;
    private boolean active = true;
}
