package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts",
       indexes = {
           @Index(name = "idx_bankacct_tenant", columnList = "tenant_id"),
           @Index(name = "idx_bankacct_number", columnList = "tenant_id, account_number")
       })
@Getter
@Setter
public class BankAccount extends TenantEntity {

    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false, length = 200)
    private String bankName;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(length = 100)
    private String branch;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 18, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(length = 10)
    private String currency = "INR";

    @Column(nullable = false)
    private boolean active = true;
}
