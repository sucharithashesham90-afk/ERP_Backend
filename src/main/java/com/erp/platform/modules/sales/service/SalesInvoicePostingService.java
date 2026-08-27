package com.erp.platform.modules.sales.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Completes a sales invoice by posting its total to the ledgers: the customer ledger is DEBITED
 * (the customer owes us) and the sales ledger is CREDITED. The customer/sales ledgers are resolved
 * by name and auto-created if they don't yet exist, so the entry always shows in Ledger Search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesInvoicePostingService {

    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final PartyLedgerService partyLedgerService;
    private final TenantContext tenantContext;

    @Transactional
    public Invoice completeInvoice(UUID id) {
        UUID tenantId = tenantContext.current();
        Invoice inv = invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Invoice not found: " + id));
        if (inv.isPosted())
            throw AppException.badRequest("Invoice is already completed and posted");

        // Idempotency check: if a voucher already exists for this invoice, mark posted and return.
        if (journalEntryService.existsByReferenceIdOrNumber(tenantId, inv.getId(), inv.getInvoiceNumber(), java.util.List.of("SALES_INVOICE", "INVOICE"))) {
            inv.setPosted(true);
            return invoiceRepository.save(inv);
        }

        BigDecimal amount = inv.getTotalAmount() == null ? BigDecimal.ZERO : inv.getTotalAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw AppException.badRequest("Invoice total must be greater than zero to post");

        Account customerLedger = resolveCustomerLedger(tenantId, inv);
        Account salesLedger = resolveSalesLedger(tenantId);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setEntryDate(inv.getInvoiceDate() != null ? inv.getInvoiceDate() : LocalDate.now());
        je.setReferenceType("SALES_INVOICE");
        je.setReferenceId(inv.getId());
        je.setReferenceNumber(inv.getInvoiceNumber());
        je.setDescription("Sales invoice " + inv.getInvoiceNumber()
                + (inv.getCustomerName() != null ? " - " + inv.getCustomerName() : ""));

        JournalEntryLine debit = new JournalEntryLine();   // customer (accounts receivable)
        debit.setAccountId(customerLedger.getId());
        debit.setAccountCode(customerLedger.getCode());
        debit.setAccountName(customerLedger.getName());
        debit.setDebitAmount(amount);
        debit.setCreditAmount(BigDecimal.ZERO);
        debit.setDescription("Sales invoice - customer");
        je.getLines().add(debit);

        JournalEntryLine credit = new JournalEntryLine();  // sales revenue
        credit.setAccountId(salesLedger.getId());
        credit.setAccountCode(salesLedger.getCode());
        credit.setAccountName(salesLedger.getName());
        credit.setCreditAmount(amount);
        credit.setDebitAmount(BigDecimal.ZERO);
        credit.setDescription("Sales invoice - sales");
        je.getLines().add(credit);

        JournalEntry saved = journalEntryService.create(je);
        journalEntryService.post(saved.getId());

        inv.setPosted(true);
        inv.setJournalEntryId(saved.getId());
        inv.setJournalEntryNumber(saved.getEntryNumber());
        if (inv.getBalanceDue() != null && inv.getBalanceDue().compareTo(BigDecimal.ZERO) == 0 && inv.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            inv.setStatus(Invoice.InvoiceStatus.PAID);
        } else {
            inv.setStatus(Invoice.InvoiceStatus.SENT);
        }
        log.info("Invoice {} completed and posted (JE {})", inv.getInvoiceNumber(), saved.getEntryNumber());
        return invoiceRepository.save(inv);
    }

    /**
     * The customer's own ledger (AR). Resolved by customer id first — using the same deterministic
     * code the party ledger uses ({@code SD-<id>}) — so the entry always lands on the exact ledger the
     * customer screen shows, never a name-matched ghost duplicate. Falls back to name for legacy
     * invoices that carry no customer id, and creates the ledger if it is genuinely absent.
     */
    private Account resolveCustomerLedger(UUID tenantId, Invoice inv) {
        UUID customerId = inv.getCustomerId();
        String customerName = inv.getCustomerName();

        if (customerId != null) {
            String code = partyLedgerService.ledgerCode(PartyLedgerService.PartyType.CUSTOMER, customerId);
            var byCode = accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code);
            if (byCode.isPresent()) return byCode.get();
            // Customer has no ledger yet (e.g. created before auto-ledgers): create the canonical one.
            if (customerName != null && !customerName.isBlank()) {
                partyLedgerService.ensureLedger(PartyLedgerService.PartyType.CUSTOMER, customerName, customerId, null);
                var created = accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code);
                if (created.isPresent()) return created.get();
            }
        }

        if (customerName == null || customerName.isBlank())
            throw AppException.badRequest("Invoice has no customer to post the ledger against");
        return accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, customerName)
                .orElseGet(() -> {
                    Account a = new Account();
                    a.setTenantId(tenantId);
                    a.setName(customerName);
                    a.setType("ASSET");
                    a.setSubType("CUSTOMER");
                    a.setCode(customerId != null
                            ? partyLedgerService.ledgerCode(PartyLedgerService.PartyType.CUSTOMER, customerId)
                            : uniqueCode(tenantId, "CUST"));
                    a.setActive(true);
                    a.setBalance(BigDecimal.ZERO);
                    return accountRepository.save(a);
                });
    }

    /** The sales revenue ledger; created if absent. */
    private Account resolveSalesLedger(UUID tenantId) {
        return accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, "SALES")
                .or(() -> accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, "Sales"))
                .orElseGet(() -> {
                    Account a = new Account();
                    a.setTenantId(tenantId);
                    a.setName("Sales");
                    a.setType("INCOME");
                    a.setSubType("SALES");
                    a.setCode("SALES");
                    a.setActive(true);
                    a.setBalance(BigDecimal.ZERO);
                    return accountRepository.save(a);
                });
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
