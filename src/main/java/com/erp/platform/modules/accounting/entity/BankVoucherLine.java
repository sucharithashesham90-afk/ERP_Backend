package com.erp.platform.modules.accounting.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bank_voucher_lines",
       indexes = {@Index(name = "idx_bvl_voucher", columnList = "bank_voucher_id")})
@Getter
@Setter
public class BankVoucherLine extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_voucher_id", nullable = false)
    @JsonIgnore
    private BankVoucher bankVoucher;

    @Column(name = "account_group", length = 100)
    private String accountGroup;

    @Column(name = "ledger_account_id")
    private UUID ledgerAccountId;

    @Column(name = "ledger_account_name", length = 200)
    private String ledgerAccountName;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "cost_center_name", length = 100)
    private String costCenterName;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "bill_number", length = 100)
    private String billNumber;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "transaction_type", length = 10)
    private String transactionType;
}
