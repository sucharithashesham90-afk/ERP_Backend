package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_applications",
       indexes = {@Index(name = "idx_lvapp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LeaveApplication extends TenantEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "leave_type_id")
    private UUID leaveTypeId;

    @Column(name = "leave_type", length = 30)
    private String leaveType;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    private int days;

    @Column(length = 1000)
    private String reason;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
