package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriLotPurchaseReturn")
@Table(name = "agri_lot_purchase_returns", indexes = {@Index(name = "idx_lpr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LotPurchaseReturn extends TenantEntity {

    @Column(name = "return_number", length = 50, nullable = false)
    private String returnNumber;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "return_quantity_kgs", precision = 15, scale = 3)
    private BigDecimal returnQuantityKgs;

    @Column(name = "return_reason", length = 50)
    private String returnReason;

    @Column(name = "original_invoice_number", length = 50)
    private String originalInvoiceNumber;

    @Column(name = "credit_note_number", length = 50)
    private String creditNoteNumber;

    @Column(name = "status", length = 20)
    private String status = "PENDING";
}
