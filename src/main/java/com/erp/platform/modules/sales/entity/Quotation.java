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

@Entity
@Table(name = "quotations",
       indexes = {
           @Index(name = "idx_quot_tenant", columnList = "tenant_id"),
           @Index(name = "idx_quot_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class Quotation extends TenantEntity {

    @Column(name = "quotation_number", nullable = false, length = 50)
    private String quotationNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "quotation_date")
    private LocalDate quotationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String terms;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> items = new ArrayList<>();

    public enum QuotationStatus {
        DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED
    }
}
