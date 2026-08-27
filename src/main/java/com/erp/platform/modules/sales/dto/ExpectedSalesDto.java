package com.erp.platform.modules.sales.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ExpectedSalesDto {

    private UUID id;
    private String cropGroup;
    private String cropName;
    private String varietyName;
    private String salesArea;
    private String salesPeriod;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal expectedSalesKgs;
    private BigDecimal expectedDealerBalanceKgs;
    private String remarks;
    private LocalDateTime createdAt;
}
