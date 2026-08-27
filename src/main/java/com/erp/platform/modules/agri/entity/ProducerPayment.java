package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producer_payments",
       indexes = {@Index(name = "idx_producer_pay_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProducerPayment extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String paymentNumber;

    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_contract_id")
    private ProducerContract producerContract;

    @Column(precision = 15, scale = 3)
    private BigDecimal deliveredQuantity;

    @Column(precision = 15, scale = 3)
    private BigDecimal qualifiedQuantity;

    @Column(precision = 15, scale = 3)
    private BigDecimal rejectedQuantity;

    @Column(precision = 15, scale = 4)
    private BigDecimal ratePerUnit;

    @Column(precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 50)
    private String referenceNumber;

    /** PENDING / PAID / PARTIAL / CANCELLED */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(length = 500)
    private String remarks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_id")
    private List<ProducerPaymentDeduction> deductionItems = new ArrayList<>();
}
