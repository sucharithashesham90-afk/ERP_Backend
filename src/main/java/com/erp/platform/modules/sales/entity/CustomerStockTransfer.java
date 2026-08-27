package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "SalesCustomerStockTransfer")
@Table(name = "sales_customer_stock_transfers",
        indexes = {@Index(name = "idx_sales_cst_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CustomerStockTransfer extends TenantEntity {

    @Column(length = 100)
    private String transferNumber;

    private LocalDate transferDate;

    @Column(length = 200)
    private String fromCustomer;

    @Column(length = 200)
    private String toCustomer;

    @Column(length = 200)
    private String location;

    @Column(nullable = false)
    private boolean freightPaid = false;

    @Column(length = 200)
    private String productName;

    @Column(precision = 12, scale = 3)
    private BigDecimal fromDispatchQuantity;

    @Column(precision = 12, scale = 3)
    private BigDecimal toDispatchQuantity;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    // ---- Lot & amounts for posting (per Sales module spec) ----

    /** Lot the stock moves on (inventory is lot-based). */
    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    /** Amount for the from-customer sales return (credit note). */
    @Column(name = "from_amount", precision = 18, scale = 2)
    private BigDecimal fromAmount;

    /** Amount for the to-customer sales invoice. */
    @Column(name = "to_amount", precision = 18, scale = 2)
    private BigDecimal toAmount;

    /** Set once the transfer is posted (return + invoice + ledgers). */
    @Column(name = "posted", nullable = false, columnDefinition = "boolean not null default false")
    private boolean posted = false;

    @Column(name = "from_journal_entry_number", length = 40)
    private String fromJournalEntryNumber;

    @Column(name = "to_journal_entry_number", length = 40)
    private String toJournalEntryNumber;

    @Column(name = "to_invoice_id")
    private UUID toInvoiceId;

    @Column(name = "to_invoice_number", length = 40)
    private String toInvoiceNumber;

    @Column(name = "from_credit_note_number", length = 40)
    private String fromCreditNoteNumber;

    // ── From Customer Address block ──
    @Column(name = "from_address1", length = 300) private String fromAddress1;
    @Column(name = "from_address2", length = 300) private String fromAddress2;
    @Column(name = "from_state", length = 120)     private String fromState;
    @Column(name = "from_district", length = 120)  private String fromDistrict;
    @Column(name = "from_city", length = 120)      private String fromCity;
    @Column(name = "from_zip", length = 20)        private String fromZip;
    @Column(name = "from_phone", length = 40)      private String fromPhone;

    // ── To Customer Address block ──
    @Column(name = "address1", length = 300) private String address1;
    @Column(name = "address2", length = 300) private String address2;
    @Column(name = "state", length = 120)    private String state;
    @Column(name = "district", length = 120) private String district;
    @Column(name = "city", length = 120)     private String city;
    @Column(name = "zip", length = 20)       private String zip;
    @Column(name = "phone", length = 40)     private String phone;

    // ── Freight ──
    @Column(name = "freight_total", precision = 18, scale = 2)   private BigDecimal freightTotal;
    @Column(name = "freight_paid_amount", precision = 18, scale = 2) private BigDecimal freightPaidAmount;
    @Column(name = "freight_to_pay", precision = 18, scale = 2)  private BigDecimal freightToPay;

    @Column(name = "description", length = 1000) private String description;

    @Column(name = "dispatch_location", length = 200) private String dispatchLocation;
    @Column(name = "dealer_stock_code", length = 100) private String dealerStockCode;

    // ── Line items ──
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sales_customer_transfer_items", joinColumns = @JoinColumn(name = "transfer_id"))
    private List<CustomerTransferLine> items = new ArrayList<>();
}
