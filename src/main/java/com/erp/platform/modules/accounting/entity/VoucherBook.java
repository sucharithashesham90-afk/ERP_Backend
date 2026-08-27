package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "voucher_books",
       indexes = {
           @Index(name = "idx_vb_tenant", columnList = "tenant_id"),
           @Index(name = "idx_vb_code",   columnList = "tenant_id,code", unique = true)
       })
@Getter
@Setter
public class VoucherBook extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "voucher_type", length = 30)
    private String voucherType; // JOURNAL, PAYMENT, RECEIPT, PURCHASE, SALES

    @Column(length = 20)
    private String prefix;

    @Column(length = 20)
    private String suffix;

    @Column(name = "start_number")
    private Integer startNumber = 1;

    @Column(name = "current_number")
    private Integer currentNumber = 1;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    // ---- Book definition (per Accounting module spec) ----

    @Column(length = 30)
    private String abbreviation;

    @Column(length = 100)
    private String period;

    @Column(name = "auto_posting")
    private Boolean autoPosting = true;

    /** Automatically post the transaction (credit/debit) to the ledger. */
    @Column(name = "auto_post_to_ledger")
    private Boolean autoPostToLedger = false;

    /** Groups this book posts under (multi-select), stored as a comma-separated list of group codes. */
    @Column(name = "group_codes", length = 500)
    private String groupCodes;

    /** How many days back a voucher may be back-dated in this book. */
    @Column(name = "allow_back_date_days")
    private Integer allowBackDateDays = 0;

    /** Whether the voucher itself is posted. */
    @Column(name = "voucher_post")
    private Boolean voucherPost = false;
}
