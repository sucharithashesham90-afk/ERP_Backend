package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.OpeningBalance.BalanceType;
import com.erp.platform.modules.accounting.entity.OpeningBalance.MigrationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OpeningBalanceDto {
    private UUID id;
    private UUID tenantId;
    private String migrationRef;
    private BalanceType balanceType;
    private LocalDate asOfDate;
    private UUID accountId;
    private String accountCode;
    private String accountName;
    private UUID partyId;
    private String partyName;
    private String partyType;
    private UUID productId;
    private String productName;
    private UUID warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String currency;
    private String reference;
    private MigrationStatus status;
    private LocalDateTime postedAt;
    private String notes;
    private LocalDateTime createdAt;
}
