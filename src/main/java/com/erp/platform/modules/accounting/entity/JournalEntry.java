package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journal_entries",
       indexes = {
           @Index(name = "idx_je_tenant", columnList = "tenant_id"),
           @Index(name = "idx_je_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class JournalEntry extends TenantEntity {

    @Column(name = "entry_number", nullable = false, length = 50)
    private String entryNumber;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    /** The voucher book this entry was numbered from (Purchase Invoices, Sales Invoices, …). */
    @Column(name = "voucher_book_id")
    private UUID voucherBookId;

    @Column(name = "voucher_book_code", length = 30)
    private String voucherBookCode;

    @Column(name = "voucher_book_name", length = 100)
    private String voucherBookName;

    /** The book's type — JOURNAL, PAYMENT, RECEIPT, PURCHASE, SALES — so reports can group by it. */
    @Column(name = "voucher_type", length = 30)
    private String voucherType;

    @Column(length = 1000)
    private String description;

    @Column(length = 15)
    @Enumerated(EnumType.STRING)
    private JEStatus status = JEStatus.DRAFT;

    @Column(name = "total_debit", precision = 18, scale = 2)
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @Column(name = "total_credit", precision = 18, scale = 2)
    private BigDecimal totalCredit = BigDecimal.ZERO;

    // TDS/VAT voucher entry fields — only populated when referenceType is TDS or VAT.
    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "party_account_id")
    private UUID partyAccountId;

    @Column(name = "tds_amount", precision = 18, scale = 2)
    private BigDecimal tdsAmount;

    @Column(name = "tds_rate", precision = 6, scale = 2)
    private BigDecimal tdsRate;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "vat_amount", precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "vat_rate", precision = 6, scale = 2)
    private BigDecimal vatRate;

    @JsonManagedReference
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<JournalEntryLine> lines = new ArrayList<>();

    public enum JEStatus {
        DRAFT, POSTED, CANCELLED
    }
}
