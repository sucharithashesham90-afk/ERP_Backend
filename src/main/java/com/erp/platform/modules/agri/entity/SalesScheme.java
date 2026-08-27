package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_schemes",
       indexes = {@Index(name = "idx_sales_scheme_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SalesScheme extends TenantEntity {

    @Column(nullable = false, length = 150)
    private String schemeName;

    @Column(length = 30)
    private String schemeCode;

    @Column(length = 50)
    private String season;

    private LocalDate validFrom;
    private LocalDate validTo;

    /** CASH_DISCOUNT / QUANTITY_DISCOUNT / SPECIAL_RATE / LOYALTY / COMBO */
    @Column(length = 30)
    private String schemeType;

    @Column(precision = 6, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal flatDiscountAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private boolean active = true;
}
