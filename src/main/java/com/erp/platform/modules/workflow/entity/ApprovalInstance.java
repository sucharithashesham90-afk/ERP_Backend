package com.erp.platform.modules.workflow.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_instances",
       indexes = {@Index(name = "idx_appr_inst_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ApprovalInstance extends TenantEntity {

    public enum ApprovalStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, ESCALATED, CANCELLED
    }

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "current_level")
    private int currentLevel = 1;

    @Column(name = "max_level")
    private int maxLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("level ASC")
    private List<ApprovalStep> steps = new ArrayList<>();
}
