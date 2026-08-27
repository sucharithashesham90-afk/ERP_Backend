package com.erp.platform.modules.workflow.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_instances",
       indexes = {@Index(name = "idx_wfinst_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WorkflowInstance extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @Column(length = 50)
    private String module;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 15)
    @Enumerated(EnumType.STRING)
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Column(name = "current_step")
    private int currentStep = 1;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowApproval> approvals = new ArrayList<>();

    public enum WorkflowStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
