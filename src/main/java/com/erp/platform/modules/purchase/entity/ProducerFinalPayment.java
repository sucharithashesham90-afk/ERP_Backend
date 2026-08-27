package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriProducerFinalPayment")
@Table(name = "agri_producer_final_payments", indexes = {@Index(name = "idx_pfp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProducerFinalPayment extends TenantEntity {

    @Column(name = "payment_number", length = 50, nullable = false)
    private String paymentNumber;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "producer_name", length = 200)
    private String producerName;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "contract_quantity_kgs", precision = 15, scale = 3)
    private BigDecimal contractQuantityKgs;

    @Column(name = "actual_supplied_kgs", precision = 15, scale = 3)
    private BigDecimal actualSuppliedKgs;

    @Column(name = "rate_per_kg", precision = 15, scale = 4)
    private BigDecimal ratePerKg;

    @Column(name = "basic_amount", precision = 15, scale = 2)
    private BigDecimal basicAmount;

    @Column(name = "deductions", precision = 15, scale = 2)
    private BigDecimal deductions;

    @Column(name = "advance_paid", precision = 15, scale = 2)
    private BigDecimal advancePaid;

    @Column(name = "net_payable", precision = 15, scale = 2)
    private BigDecimal netPayable;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";
}
