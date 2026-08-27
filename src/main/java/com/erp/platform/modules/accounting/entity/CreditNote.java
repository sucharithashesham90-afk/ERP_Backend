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
@Table(name = "credit_notes",
       indexes = {
           @Index(name = "idx_cn_tenant", columnList = "tenant_id"),
           @Index(name = "idx_cn_date",   columnList = "tenant_id, note_date")
       })
@Getter
@Setter
public class CreditNote extends TenantEntity {

    @Column(name = "credit_note_number", nullable = false, length = 50)
    private String creditNoteNumber;

    @Column(name = "note_type", length = 20)
    private String noteType = "DIRECT"; // DIRECT, SALES_RETURN

    @Column(name = "note_date")
    private LocalDate noteDate;

    @Column(name = "bill_no", length = 50)
    private String billNo;

    @Column(name = "sales_return_id")
    private UUID salesReturnId;

    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "narration", length = 1000)
    private String narration;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "DRAFT"; // DRAFT, POSTED

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditNoteLine> lines = new ArrayList<>();
}
