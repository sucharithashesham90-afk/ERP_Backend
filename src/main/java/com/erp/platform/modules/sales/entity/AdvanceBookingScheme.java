package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "SalesAdvanceBookingScheme")
@Table(name = "sales_advance_booking_schemes", indexes = {
    @Index(name = "idx_advance_booking_scheme_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class AdvanceBookingScheme extends TenantEntity {

    @Column(name = "scheme_code", length = 50, nullable = false)
    private String schemeCode;

    @Column(name = "scheme_name", length = 200)
    private String schemeName;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "benefit_type", length = 50)
    private String benefitType;

    @Column(name = "flat_benefit_percent", precision = 5, scale = 2)
    private BigDecimal flatBenefitPercent;

    @Column(name = "min_order_qty", precision = 15, scale = 3)
    private BigDecimal minOrderQty;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
}
