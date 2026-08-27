package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "producer_advances",
       indexes = {@Index(name = "idx_prod_adv_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProducerAdvance extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String advanceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_contract_id")
    private ProducerContract producerContract;

    @Column(nullable = false)
    private LocalDate advanceDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal advanceAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal adjustedAmount = BigDecimal.ZERO;

    @Column(length = 30)
    private String paymentMethod; // CASH, CHEQUE, BANK_TRANSFER

    @Column(length = 50)
    private String referenceNumber;

    @Column(length = 30)
    private String status; // PENDING, PAID, ADJUSTED, CANCELLED

    @Column(length = 500)
    private String remarks;
}
