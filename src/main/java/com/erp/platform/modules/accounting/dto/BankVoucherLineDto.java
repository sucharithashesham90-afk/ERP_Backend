package com.erp.platform.modules.accounting.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BankVoucherLineDto {
    private UUID id;
    private String accountGroup;
    private UUID ledgerAccountId;
    private String ledgerAccountName;
    private UUID costCenterId;
    private String costCenterName;
    private BigDecimal amount;
    private String billNumber;
    private String locationName;
    private String transactionType;
}
