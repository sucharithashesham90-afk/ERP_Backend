package com.erp.platform.modules.expense.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses",
       indexes = {
           @Index(name = "idx_expense_tenant", columnList = "tenant_id"),
           @Index(name = "idx_expense_employee", columnList = "tenant_id, employee_id"),
           @Index(name = "idx_expense_status", columnList = "tenant_id, status")
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

    @Column(length = 50)
    private String category; // TRAVEL, ACCOMMODATION, FOOD, SUPPLIES, COMMUNICATION, OTHER

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "INR";

    @Column(length = 500)
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "receipt_reference", length = 100)
    private String receiptReference;

    @Column(length = 1000)
    private String notes;

    public enum ExpenseStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, PAID
    }
}
