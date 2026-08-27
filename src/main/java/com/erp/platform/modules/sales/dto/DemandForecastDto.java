package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.DemandForecast.ForecastMethod;
import com.erp.platform.modules.sales.entity.DemandForecast.ForecastStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DemandForecastDto {
    private UUID id;
    private UUID tenantId;
    private String forecastNumber;
    private UUID productId;
    private String productName;
    private String forecastPeriod;
    private int forecastYear;
    private Integer forecastMonth;
    private Integer forecastQuarter;
    private ForecastMethod forecastMethod;
    private BigDecimal forecastedQty;
    private BigDecimal actualQty;
    private BigDecimal variance;
    private int confidenceLevel;
    private UUID warehouseId;
    private String unit;
    private ForecastStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
