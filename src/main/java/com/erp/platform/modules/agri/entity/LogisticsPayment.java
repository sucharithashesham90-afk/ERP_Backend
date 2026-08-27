package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "logistics_payments",
       indexes = {@Index(name = "idx_logistics_pay_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LogisticsPayment extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String paymentNumber;

    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_contract_id")
    private ProducerContract producerContract;

    @Column(length = 100)
    private String logisticsProvider;

    @Column(length = 50)
    private String vehicleNumber;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantityHandled;

    @Column(length = 20)
    private String handlingUom;

    @Column(precision = 15, scale = 4)
    private BigDecimal ratePerUnit;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 50)
    private String referenceNumber;

    /** PENDING / PAID / CANCELLED */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(length = 500)
    private String remarks;
}
