package com.erp.platform.modules.sales.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Shared sales-side ledger helpers: resolves (and auto-creates) the customer / sales / sales-returns
 * ledgers and posts balanced two-line journal entries, so sales postings all show in Ledger Search.
 */
@Service
@RequiredArgsConstructor
public class SalesLedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;

    /** Customer's own ledger (accounts receivable); created on first use. */
    public Account resolveCustomerLedger(UUID tenantId, String customerName) {
        if (customerName == null || customerName.isBlank())
            throw AppException.badRequest("No customer to post the ledger against");
        return accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, customerName)
                .orElseGet(() -> createAccount(tenantId, customerName, "ASSET", "CUSTOMER", uniqueCode(tenantId, "CUST")));
    }

    /** Sales revenue ledger. */
    public Account resolveSalesLedger(UUID tenantId) {
        return resolveByCodeOrName(tenantId, "SALES", "Sales", "INCOME", "SALES");
    }

    /** Sales returns (contra-revenue) ledger. */
    public Account resolveSalesReturnsLedger(UUID tenantId) {
        return resolveByCodeOrName(tenantId, "SALES_RET", "Sales Returns", "EXPENSE", "SALES_RETURNS");
    }

    /** Create and POST a balanced entry: {@code debit} is debited and {@code credit} credited by {@code amount}. */
    public JournalEntry postEntry(Account debit, Account credit, BigDecimal amount,
                                  String refType, UUID refId, String refNumber, String description, LocalDate date) {
        JournalEntry je = new JournalEntry();
        je.setEntryDate(date != null ? date : LocalDate.now());
        je.setReferenceType(refType);
        je.setReferenceId(refId);
        je.setReferenceNumber(refNumber);
        je.setDescription(description);
        je.getLines().add(line(debit, amount, BigDecimal.ZERO, "Debit"));
        je.getLines().add(line(credit, BigDecimal.ZERO, amount, "Credit"));
        JournalEntry saved = journalEntryService.create(je);
        journalEntryService.post(saved.getId());
        return saved;
    }

    private JournalEntryLine line(Account a, BigDecimal debit, BigDecimal credit, String note) {
        JournalEntryLine l = new JournalEntryLine();
        l.setAccountId(a.getId());
        l.setAccountCode(a.getCode());
        l.setAccountName(a.getName());
        l.setDebitAmount(debit);
        l.setCreditAmount(credit);
        l.setDescription(note + " - " + a.getName());
        return l;
    }

    private Account resolveByCodeOrName(UUID tenantId, String code, String name, String type, String subType) {
        return accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .or(() -> accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name))
                .orElseGet(() -> createAccount(tenantId, name, type, subType, code));
    }

    private Account createAccount(UUID tenantId, String name, String type, String subType, String code) {
        Account a = new Account();
        a.setTenantId(tenantId);
        a.setName(name);
        a.setType(type);
        a.setSubType(subType);
        a.setCode(code);
        a.setActive(true);
        a.setBalance(BigDecimal.ZERO);
        return accountRepository.save(a);
    }

    private String uniqueCode(UUID tenantId, String prefix) {
        long base = System.currentTimeMillis() % 100000;
        for (int i = 0; i < 1000; i++) {
            String code = prefix + "-" + (base + i);
            if (accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code).isEmpty())
                return code;
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
