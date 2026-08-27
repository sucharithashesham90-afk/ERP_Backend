package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Vendor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_advances",
       indexes = {
           @Index(name = "idx_sadv_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sadv_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class SupplierAdvance extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vendor vendor;

    @Column(name = "advance_date", nullable = false)
    private LocalDate advanceDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "advance_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdvanceType advanceType = AdvanceType.BANK_TRANSFER;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private AdvanceStatus status = AdvanceStatus.PENDING;

    @Column(name = "adjusted_amount", precision = 18, scale = 2)
    private BigDecimal adjustedAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @Transient
    private BigDecimal balanceAmount;

    @PostLoad
    public void computeBalance() {
        BigDecimal adj = adjustedAmount != null ? adjustedAmount : BigDecimal.ZERO;
        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        this.balanceAmount = amt.subtract(adj);
    }

    public enum AdvanceType {
        CASH, BANK_TRANSFER, CHEQUE, MATERIAL
    }

    public enum AdvanceStatus {
        PENDING, PARTIALLY_ADJUSTED, FULLY_ADJUSTED, CANCELLED
    }
}
