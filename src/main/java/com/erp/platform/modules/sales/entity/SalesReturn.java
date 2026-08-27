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
@Table(name = "sales_returns",
       indexes = {@Index(name = "idx_sr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SalesReturn extends TenantEntity {

    @Column(name = "return_number", nullable = false, length = 50)
    private String returnNumber;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

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

    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "UNPAID";

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "cheque_number", length = 100)
    private String chequeNumber;

    @Column(name = "cheque_date", length = 50)
    private String chequeDate;

    @OneToMany(mappedBy = "salesReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesReturnItem> items = new ArrayList<>();
}
