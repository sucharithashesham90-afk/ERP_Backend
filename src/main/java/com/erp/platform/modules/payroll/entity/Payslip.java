package com.erp.platform.modules.payroll.entity;

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
@Table(name = "payslips",
       indexes = {@Index(name = "idx_payslip_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Payslip extends TenantEntity {

    @Column(name = "payslip_number", nullable = false, length = 50)
    private String payslipNumber;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "pay_month")
    private int payMonth;

    @Column(name = "pay_year")
    private int payYear;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Column(name = "basic_salary", precision = 18, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "gross_earnings", precision = 18, scale = 2)
    private BigDecimal grossEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 18, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 18, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(name = "working_days")
    private int workingDays;

    @Column(name = "present_days")
    private int presentDays;

    @Column(name = "leave_days")
    private int leaveDays;

    @Column(length = 15)
    @Enumerated(EnumType.STRING)
    private PayslipStatus status = PayslipStatus.DRAFT;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayslipLine> lines = new ArrayList<>();

    public enum PayslipStatus {
        DRAFT, APPROVED, PAID, REJECTED, CANCELLED
    }
}
