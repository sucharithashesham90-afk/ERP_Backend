package com.erp.platform.modules.accounting.entity;

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
@Table(name = "bank_vouchers",
       indexes = {
           @Index(name = "idx_bv_tenant",    columnList = "tenant_id"),
           @Index(name = "idx_bv_type_date", columnList = "tenant_id, voucher_type, voucher_date")
       })
@Getter
@Setter
public class BankVoucher extends TenantEntity {

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "voucher_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private VoucherType voucherType;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @Column(name = "narration", length = 1000)
    private String narration;

    @Column(name = "bank_account_id")
    private UUID bankAccountId;

    @Column(name = "bank_account_name", length = 200)
    private String bankAccountName;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_mode", length = 20)
    private String paymentMode;

    @Column(name = "cheque_number", length = 50)
    private String chequeNumber;

    @Column(name = "cheque_date")
    private LocalDate chequeDate;

    @Column(name = "single_cheque_print")
    private boolean singleChequePrint = false;

    @Column(name = "multi_cheque")
    private boolean multiCheque = false;

    @Column(name = "is_rtgs")
    private boolean rtgs = false;

    @Column(name = "is_ac_payee")
    private boolean acPayee = false;

    @Column(name = "reference_voucher_number", length = 50)
    private String referenceVoucherNumber;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @OneToMany(mappedBy = "bankVoucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdAt ASC")
    private List<BankVoucherLine> lines = new ArrayList<>();

    public enum VoucherType {
        RECEIPT, PAYMENT
    }
}
