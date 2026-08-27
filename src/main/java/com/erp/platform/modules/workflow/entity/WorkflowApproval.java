package com.erp.platform.modules.workflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_approvals")
@Getter
@Setter
public class WorkflowApproval extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    @JsonIgnore
    private WorkflowInstance instance;

    @Column(name = "step_order")
    private int stepOrder;

    @Column(name = "step_name", length = 100)
    private String stepName;

    @Column(name = "approver_id")
    private UUID approverId;

    @Column(length = 15)
    private String action; // APPROVED, REJECTED, DELEGATED

    @Column(length = 1000)
    private String comments;

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Column(length = 15)
    private String status; // PENDING, WAITING, COMPLETED
}
