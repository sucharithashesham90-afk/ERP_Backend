package com.erp.platform.modules.supplier.dto;

import com.erp.platform.modules.supplier.entity.SupplierPerformance.PerfStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SupplierPerformanceDto {

    private UUID id;
    private UUID tenantId;
    private UUID vendorId;
    private String vendorName;
    private String evaluationPeriod;
    private LocalDate evaluationDate;
    private BigDecimal onTimeDeliveryScore;
    private BigDecimal qualityScore;
    private BigDecimal responseScore;
    private BigDecimal pricingScore;
    private BigDecimal overallScore;
    private int totalOrders;
    private int onTimeOrders;
    private int lateOrders;
    private int rejectedLots;
    private int acceptedLots;
    private PerfStatus status;
    private String remarks;
    private String evaluatedBy;
    private LocalDateTime createdAt;
}
