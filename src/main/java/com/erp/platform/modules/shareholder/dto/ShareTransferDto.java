package com.erp.platform.modules.shareholder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShareTransferDto {
    private UUID id;
    private String transferNumber;
    private UUID fromShareholderId;
    private String fromShareholderName;
    private UUID toShareholderId;
    private String toShareholderName;
    private LocalDate transferDate;
    private BigDecimal sharesTransferred;
    private BigDecimal transferPricePerShare;
    private BigDecimal stampDuty;
    private String transferType;
    private String instrumentNumber;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
