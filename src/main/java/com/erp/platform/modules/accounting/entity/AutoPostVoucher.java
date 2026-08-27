package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "auto_post_vouchers",
       indexes = {@Index(name = "idx_apv_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AutoPostVoucher extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String triggerType; // DAILY, WEEKLY, MONTHLY, ON_DATE

    @Column(name = "debit_ledger_id")
    private UUID debitLedgerId;

    @Column(name = "credit_ledger_id")
    private UUID creditLedgerId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(length = 500)
    private String narration;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "next_run_date")
    private java.time.LocalDate nextRunDate;

    @Column(name = "last_run_date")
    private java.time.LocalDate lastRunDate;

    @Column(nullable = false)
    private boolean active = true;
}
