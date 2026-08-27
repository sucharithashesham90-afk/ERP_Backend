package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expected_sales", indexes = {@Index(name = "idx_expected_sales_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ExpectedSales extends TenantEntity {

    @Column(name = "crop_group", length = 100)
    private String cropGroup;

    @Column(name = "crop_name", length = 150)
    private String cropName;

    @Column(name = "variety_name", length = 150)
    private String varietyName;

    /** Sales Area (from the Sales Areas screen) — was a free-text "location" field. */
    @Column(name = "sales_area", length = 200)
    private String salesArea;

    /** Sales Period (from Sales Period Setup, periodType=SALES) — was a free-text "season" field. */
    @Column(name = "sales_period", length = 100)
    private String salesPeriod;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "expected_sales_kgs", precision = 15, scale = 3)
    private BigDecimal expectedSalesKgs;

    @Column(name = "expected_dealer_balance_kgs", precision = 15, scale = 3)
    private BigDecimal expectedDealerBalanceKgs;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
