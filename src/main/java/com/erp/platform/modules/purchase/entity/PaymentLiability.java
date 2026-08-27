package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriPaymentLiability")
@Table(name = "agri_payment_liabilities", indexes = {@Index(name = "idx_pl_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PaymentLiability extends TenantEntity {

    @Column(name = "liability_number", length = 50, nullable = false)
    private String liabilityNumber;

    @Column(name = "party_type", length = 50)
    private String partyType;

    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "liability_from_date")
    private LocalDate liabilityFromDate;

    @Column(name = "liability_to_date")
    private LocalDate liabilityToDate;

    @Column(name = "total_liability", precision = 15, scale = 2)
    private BigDecimal totalLiability;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "status", length = 20)
    private String status = "PENDING";
}
