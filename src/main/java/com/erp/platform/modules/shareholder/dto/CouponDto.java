package com.erp.platform.modules.shareholder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CouponDto {
    private UUID id;
    private String couponNumber;
    private UUID shareholderId;
    private String shareholderName;
    private BigDecimal sharesCount;
    private BigDecimal faceValuePerShare;
    private BigDecimal dividendPercent;
    private BigDecimal dividendAmount;
    private LocalDate issueDate;
    private LocalDate maturityDate;
    private String financialYear;
    private String status;
    private LocalDate paidDate;
    private String paymentReference;
    private String remarks;
    private LocalDateTime createdAt;
}
