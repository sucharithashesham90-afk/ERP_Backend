package com.erp.platform.modules.accounting.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit_note_lines")
@Getter
@Setter
public class CreditNoteLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_note_id", nullable = false)
    @JsonIgnore
    private CreditNote creditNote;

    @Column(length = 10)
    private String side; // DEBIT, CREDIT

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "account_code", length = 20)
    private String accountCode;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 500)
    private String narration;

    @Column(name = "bill_no", length = 50)
    private String billNo;
}
