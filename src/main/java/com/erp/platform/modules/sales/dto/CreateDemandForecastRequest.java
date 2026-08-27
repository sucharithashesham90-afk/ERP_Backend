package com.erp.platform.modules.sales.dto;

import com.erp.platform.modules.sales.entity.DemandForecast.ForecastMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateDemandForecastRequest {

    private UUID productId;

    private String productName;

    private String forecastPeriod;

    @NotNull
    private Integer forecastYear;

    private Integer forecastMonth;

    private Integer forecastQuarter;

    @NotNull
    private ForecastMethod forecastMethod;

    @NotNull
    private BigDecimal forecastedQty;

    private BigDecimal actualQty = BigDecimal.ZERO;

    @Min(0) @Max(100)
    private int confidenceLevel = 80;

    private UUID warehouseId;

    private String unit;

    private String notes;
}
