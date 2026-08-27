package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "HrExpense")
@Table(name = "hr_expenses",
       indexes = {
           @Index(name = "idx_hr_exp_tenant", columnList = "tenant_id"),
           @Index(name = "idx_hr_exp_employee", columnList = "tenant_id, employee_id")
       })
@Getter
@Setter
public class Expense extends TenantEntity {

    @Column(name = "expense_number", nullable = false, length = 50)
    private String expenseNumber;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "receipt_reference", length = 100)
    private String receiptReference;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_on")
    private LocalDate approvedOn;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reimbursed_on")
    private LocalDate reimbursedOn;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum ExpenseStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, REIMBURSED
    }
}
