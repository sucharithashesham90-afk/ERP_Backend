package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "demand_forecasts",
       indexes = {
           @Index(name = "idx_df_tenant", columnList = "tenant_id"),
           @Index(name = "idx_df_product", columnList = "tenant_id, product_id"),
           @Index(name = "idx_df_year", columnList = "tenant_id, forecast_year"),
           @Index(name = "idx_df_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class DemandForecast extends TenantEntity {

    @Column(name = "forecast_number", nullable = false, length = 50)
    private String forecastNumber;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "forecast_period", length = 20)
    private String forecastPeriod;

    @Column(name = "forecast_year", nullable = false)
    private int forecastYear;

    @Column(name = "forecast_month")
    private Integer forecastMonth;

    @Column(name = "forecast_quarter")
    private Integer forecastQuarter;

    @Column(name = "forecast_method", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ForecastMethod forecastMethod;

    @Column(name = "forecasted_qty", precision = 18, scale = 4)
    private BigDecimal forecastedQty = BigDecimal.ZERO;

    @Column(name = "actual_qty", precision = 18, scale = 4)
    private BigDecimal actualQty = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(name = "confidence_level")
    private int confidenceLevel;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ForecastStatus status = ForecastStatus.DRAFT;

    @Column(length = 1000)
    private String notes;

    public enum ForecastMethod {
        HISTORICAL, MARKET_RESEARCH, MANUAL, MOVING_AVERAGE, EXPONENTIAL_SMOOTHING
    }

    public enum ForecastStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
