package com.erp.platform.modules.purchase.entity;

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
@Table(name = "purchase_returns",
       indexes = {@Index(name = "idx_pret_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PurchaseReturn extends TenantEntity {

    @Column(name = "return_number", nullable = false, length = 50)
    private String returnNumber;

    @Column(name = "goods_receipt_id")
    private UUID goodsReceiptId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "DRAFT";

    // ── Return logistics header (per Purchase doc) ──
    @Column(name = "return_type", length = 30)
    private String returnType;              // seed / consumable / packing material
    @Column(name = "pr_location", length = 150)
    private String prLocation;
    @Column(name = "way_bill_number", length = 100)
    private String wayBillNumber;
    @Column(name = "return_value", length = 50)
    private String returnValue;
    @Column(name = "rr_rl_number", length = 100)
    private String rrRlNumber;
    @Column(length = 150)
    private String carrier;
    @Column(name = "lorry_number", length = 50)
    private String lorryNumber;
    @Column(name = "freight_total", length = 50)
    private String freightTotal;
    @Column(name = "freight_paid_advance", length = 50)
    private String freightPaidAdvance;
    @Column(name = "freight_to_pay", length = 50)
    private String freightToPay;
    // billing address
    @Column(name = "billing_address", length = 500)
    private String billingAddress;
    @Column(name = "billing_state", length = 100)
    private String billingState;
    @Column(name = "billing_district", length = 100)
    private String billingDistrict;
    @Column(name = "billing_city", length = 100)
    private String billingCity;
    @Column(name = "billing_zip", length = 20)
    private String billingZip;
    @Column(name = "billing_phone", length = 30)
    private String billingPhone;
    // delivery address
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;
    @Column(name = "delivery_state", length = 100)
    private String deliveryState;
    @Column(name = "delivery_district", length = 100)
    private String deliveryDistrict;
    @Column(name = "delivery_city", length = 100)
    private String deliveryCity;
    @Column(name = "delivery_zip", length = 20)
    private String deliveryZip;
    @Column(name = "delivery_phone", length = 30)
    private String deliveryPhone;

    // Debit note tracking
    @Column(name = "debit_note_number", length = 50)
    private String debitNoteNumber;

    @Column(name = "debit_note_date")
    private LocalDate debitNoteDate;

    @Column(name = "debit_note_status", length = 20)
    private String debitNoteStatus = "NOT_ISSUED";

    /** The posted voucher the debit note produced, so the note can be traced to the ledger. */
    @Column(name = "debit_note_je_id")
    private UUID debitNoteJournalEntryId;

    @Column(name = "debit_note_je_number", length = 50)
    private String debitNoteJournalEntryNumber;

    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseReturnItem> items = new ArrayList<>();
}
