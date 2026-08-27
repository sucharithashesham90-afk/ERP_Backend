package com.erp.platform.modules.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateJournalEntryRequest {
    private LocalDate entryDate;
    private String description;
    private String referenceType;
    private String referenceNumber;
    private List<LineRequest> lines;

    // TDS/VAT voucher entry fields — only populated when referenceType is TDS or VAT.
    private String partyName;
    private UUID partyAccountId;
    private BigDecimal tdsAmount;
    private BigDecimal tdsRate;
    private String panNumber;
    private String remarks;
    private BigDecimal vatAmount;
    private BigDecimal vatRate;

    @Data
    public static class LineRequest {
        // flat format (new frontend)
        private UUID accountId;
        private String accountCode;
        private String accountName;
        // nested format (old frontend: { account: { id, code, name } })
        private AccountRef account;

        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private String description;

        public UUID resolvedAccountId() {
            return accountId != null ? accountId : (account != null ? account.getId() : null);
        }
        public String resolvedAccountCode() {
            return accountCode != null ? accountCode : (account != null ? account.getCode() : null);
        }
        public String resolvedAccountName() {
            return accountName != null ? accountName : (account != null ? account.getName() : null);
        }

        @Data
        public static class AccountRef {
            private UUID id;
            private String code;
            private String name;
        }
    }
}
