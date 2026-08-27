package com.erp.platform.modules.workflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_steps",
       indexes = {@Index(name = "idx_appr_step_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ApprovalStep extends TenantEntity {

    public enum StepStatus {
        PENDING, APPROVED, REJECTED, ESCALATED, SKIPPED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    @JsonIgnore
    private ApprovalInstance instance;

    @Column(name = "level")
    private int level;

    @Column(name = "approver_type", length = 50)
    private String approverType;

    @Column(name = "approver_value", length = 200)
    private String approverValue;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(length = 1000)
    private String comments;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
}
