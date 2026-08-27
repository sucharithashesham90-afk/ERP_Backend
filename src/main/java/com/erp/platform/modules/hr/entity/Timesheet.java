package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A day's work recorded by an employee.
 *
 * <p>Kept against the employee rather than any one activity, because not all of a day is spent on
 * the same kind of thing: a meeting and a training session are time worked just as much as the job
 * itself, and a day that only counts one of them never adds up.
 */
@Entity
@Table(name = "hr_timesheets",
       indexes = {
           @Index(name = "idx_hr_timesheet_tenant", columnList = "tenant_id"),
           @Index(name = "idx_hr_timesheet_employee", columnList = "tenant_id, employee_id"),
       })
@Getter
@Setter
public class Timesheet extends TenantEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(precision = 6, scale = 2)
    private BigDecimal hours = BigDecimal.ZERO;

    /** WORK, MEETING, TRAINING, SUPPORT, ADMIN, TRAVEL, OTHER. */
    @Column(name = "activity_type", length = 30)
    private String activityType;

    @Column(length = 1000)
    private String description;

    /** DRAFT | SUBMITTED | APPROVED | REJECTED. */
    @Column(length = 20)
    private String status = "DRAFT";

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
