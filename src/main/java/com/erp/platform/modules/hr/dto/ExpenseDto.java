package com.erp.platform.modules.hr.dto;

import com.erp.platform.modules.hr.entity.Expense.ExpenseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExpenseDto {
    private UUID id;
    private UUID tenantId;
    private String expenseNumber;
    private UUID employeeId;
    private String employeeName;
    private LocalDate expenseDate;
    private String category;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String receiptReference;
    private ExpenseStatus status;
    private UUID approvedBy;
    private LocalDate approvedOn;
    private String rejectionReason;
    private LocalDate reimbursedOn;
    private String notes;
    private LocalDateTime createdAt;
}
