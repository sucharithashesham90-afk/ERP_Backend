package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "journal_entry_lines")
@Getter
@Setter
public class JournalEntryLine extends AuditableEntity {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_code", length = 20)
    private String accountCode;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "debit_amount", precision = 18, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "credit_amount", precision = 18, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;
}
