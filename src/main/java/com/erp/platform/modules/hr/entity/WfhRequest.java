package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** A work-from-home request with an approval workflow (mirrors leave applications, but does not consume leave balance). */
@Entity
@Table(name = "hr_wfh_requests",
       indexes = {@Index(name = "idx_wfh_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WfhRequest extends TenantEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    private int days;

    @Column(length = 1000)
    private String reason;

    /** PENDING | APPROVED | REJECTED | CANCELLED */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
