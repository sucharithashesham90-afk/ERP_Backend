package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.BankVoucher;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BankVoucherDto {
    private UUID id;
    private UUID tenantId;
    private String voucherNumber;
    private BankVoucher.VoucherType voucherType;
    private LocalDate voucherDate;
    private String partyName;
    private String purpose;
    private String narration;
    private UUID bankAccountId;
    private String bankAccountName;
    private BigDecimal totalAmount;
    private String paymentMode;
    private String chequeNumber;
    private LocalDate chequeDate;
    private boolean singleChequePrint;
    private boolean multiCheque;
    private boolean rtgs;
    private boolean acPayee;
    private String referenceVoucherNumber;
    private String status;
    private String locationName;
    private UUID journalEntryId;
    private List<BankVoucherLineDto> lines;
    private LocalDateTime createdAt;
}
