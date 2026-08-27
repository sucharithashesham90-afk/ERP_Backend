package com.erp.platform.modules.workflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
public class WorkflowStep extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    private WorkflowDefinition workflow;

    @Column(name = "step_order")
    private int stepOrder;

    @Column(name = "step_name", length = 100)
    private String stepName;

    @Column(name = "approver_role_id")
    private UUID approverRoleId;

    @Column(name = "approver_user_id")
    private UUID approverUserId;

    @Column(name = "timeout_days")
    private int timeoutDays = 3;
}
