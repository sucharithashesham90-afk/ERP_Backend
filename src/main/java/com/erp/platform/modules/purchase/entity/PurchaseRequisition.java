package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_requisitions",
       indexes = {
           @Index(name = "idx_preq_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pr_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class PurchaseRequisition extends TenantEntity {

    @Column(name = "requisition_number", nullable = false, length = 50)
    private String requisitionNumber;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "department_name", length = 200)
    private String departmentName;

    @Column(name = "required_by_date")
    private LocalDate requiredByDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ReqStatus status = ReqStatus.DRAFT;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Column(name = "total_estimated_value", precision = 18, scale = 2)
    private BigDecimal totalEstimatedValue = BigDecimal.ZERO;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseRequisitionItem> items = new ArrayList<>();

    public enum ReqStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, PO_CREATED, CANCELLED
    }

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
}
