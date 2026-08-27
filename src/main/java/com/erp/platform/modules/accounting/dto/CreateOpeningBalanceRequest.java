package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.OpeningBalance.BalanceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateOpeningBalanceRequest {

    @NotNull
    private BalanceType balanceType;

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
    private BigDecimal debitAmount = BigDecimal.ZERO;
    private BigDecimal creditAmount = BigDecimal.ZERO;
    private String currency = "INR";
    private String reference;
    private String notes;
}
