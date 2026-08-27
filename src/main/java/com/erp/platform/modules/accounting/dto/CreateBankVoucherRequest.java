package com.erp.platform.modules.accounting.dto;

import com.erp.platform.modules.accounting.entity.BankVoucher;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateBankVoucherRequest {
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
    private String locationName;
    private boolean postImmediately;
    private List<LineRequest> lines;

    @Data
    public static class LineRequest {
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
}
