package com.erp.platform.modules.shareholder.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "share_coupons",
       indexes = {@Index(name = "idx_coupon_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_coupon_sh", columnList = "shareholder_id")})
@Getter
@Setter
public class Coupon extends TenantEntity {

    @Column(name = "coupon_number", length = 50, nullable = false)
    private String couponNumber;

    @Column(name = "shareholder_id", nullable = false)
    private UUID shareholderId;

    @Column(name = "shareholder_name", length = 200)
    private String shareholderName;

    @Column(name = "shares_count", precision = 15, scale = 0)
    private BigDecimal sharesCount;

    @Column(name = "face_value_per_share", precision = 15, scale = 4)
    private BigDecimal faceValuePerShare;

    @Column(name = "dividend_percent", precision = 8, scale = 4)
    private BigDecimal dividendPercent;

    @Column(name = "dividend_amount", precision = 15, scale = 2)
    private BigDecimal dividendAmount;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "financial_year", length = 20)
    private String financialYear;

    /** ISSUED / PAID / EXPIRED / CANCELLED */
    @Column(name = "status", length = 30)
    private String status = "ISSUED";

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
