package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product_allotments",
       indexes = {@Index(name = "idx_prod_allot_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProductAllotment extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String allotmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_variant_id")
    private PlantVariant plantVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_scheme_id")
    private SalesScheme salesScheme;

    private LocalDate allotmentDate;

    @Column(length = 50)
    private String season;

    @Column(precision = 15, scale = 3)
    private BigDecimal allottedQuantity;

    @Column(length = 20)
    private String allottedUom;

    @Column(precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(precision = 6, scale = 2)
    private BigDecimal specialDiscountPercent = BigDecimal.ZERO;

    /** PENDING / CONFIRMED / PARTIAL / COMPLETED / CANCELLED */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(length = 500)
    private String remarks;
}
