package com.erp.platform.modules.supplier.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateSupplierPerformanceRequest {

    private UUID vendorId;
    private String vendorName;
    private String evaluationPeriod;
    private LocalDate evaluationDate;
    private BigDecimal onTimeDeliveryScore;
    private BigDecimal qualityScore;
    private BigDecimal responseScore;
    private BigDecimal pricingScore;
    private int totalOrders;
    private int onTimeOrders;
    private int lateOrders;
    private int rejectedLots;
    private int acceptedLots;
    private String remarks;
    private String evaluatedBy;
}
