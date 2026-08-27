package com.erp.platform.modules.sales.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.sales.entity.CustomerStockTransfer;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.CustomerStockTransferRepository;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Posts a dealer-to-dealer stock transfer. From the FROM customer: a sales return — stock is received
 * back to the lot and a credit note is posted (Dr Sales Returns, Cr from-customer). To the TO customer:
 * a stock issue — stock leaves the lot, a sales invoice is created and posted (Dr to-customer, Cr Sales).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealerTransferPostingService {

    private final CustomerStockTransferRepository repository;
    private final InvoiceRepository invoiceRepository;
    private final SalesStockService salesStockService;
    private final SalesLedgerService salesLedgerService;
    /** The transfer names its parties; an invoice needs the customer they refer to. */
    private final com.erp.platform.modules.master.repository.CustomerRepository customerRepository;
    private final TenantContext tenantContext;

    @Transactional
    public CustomerStockTransfer post(UUID id) {
        UUID tenantId = tenantContext.current();
        CustomerStockTransfer t = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        if (t.isPosted())
            throw AppException.badRequest("Transfer is already posted");
        if (t.getFromCustomer() == null || t.getFromCustomer().isBlank()
                || t.getToCustomer() == null || t.getToCustomer().isBlank())
            throw AppException.badRequest("Both from-customer and to-customer are required");

        // Resolved up front, before any stock moves. invoices.customer_id is NOT NULL and a
        // transfer only carries the party as a name, so this is the one thing here that can fail
        // for a reason the user can act on — and it should say so before doing any work, not after.
        UUID toCustomerId = customerRepository
                .findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, t.getToCustomer().trim())
                .map(c -> c.getId())
                .orElseThrow(() -> AppException.badRequest(
                        "No customer named '" + t.getToCustomer() + "' exists, so no invoice can be raised. "
                        + "Add them under Customers, or correct the To Customer on this transfer."));

        LocalDate date = t.getTransferDate() != null ? t.getTransferDate() : LocalDate.now();
        BigDecimal fromAmount = t.getFromAmount() == null ? BigDecimal.ZERO : t.getFromAmount();
        BigDecimal toAmount = t.getToAmount() == null ? BigDecimal.ZERO : t.getToAmount();

        // 1) FROM customer — sales return: receive stock back for each line + credit note.
        if (t.getItems() != null && !t.getItems().isEmpty()) {
            for (var l : t.getItems())
                salesStockService.receiveToLot(tenantId, l.getLotNumber(), nz(l.getPacks()),
                        l.getProductName(), "DEALER_TRANSFER_RETURN");
        } else {
            salesStockService.receiveToLot(tenantId, t.getLotNumber(), t.getFromDispatchQuantity(),
                    t.getProductName(), "DEALER_TRANSFER_RETURN");
        }
        if (fromAmount.compareTo(BigDecimal.ZERO) > 0) {
            Account salesReturns = salesLedgerService.resolveSalesReturnsLedger(tenantId);
            Account fromLedger = salesLedgerService.resolveCustomerLedger(tenantId, t.getFromCustomer());
            JournalEntry je = salesLedgerService.postEntry(salesReturns, fromLedger, fromAmount,
                    "DEALER_TRANSFER_RETURN", t.getId(), t.getTransferNumber(),
                    "Dealer transfer return " + t.getTransferNumber() + " — " + t.getFromCustomer(), date);
            t.setFromJournalEntryNumber(je.getEntryNumber());
        }

        // 2) TO customer — stock issue for each line (net of damaged) + sales invoice.
        if (t.getItems() != null && !t.getItems().isEmpty()) {
            for (var l : t.getItems()) {
                BigDecimal issueQty = nz(l.getPacks()).subtract(nz(l.getPacksDamaged()));
                if (issueQty.compareTo(BigDecimal.ZERO) > 0)
                    salesStockService.issueFromLot(tenantId, l.getLotNumber(), issueQty);
            }
        } else {
            salesStockService.issueFromLot(tenantId, t.getLotNumber(), t.getToDispatchQuantity());
        }

        Account salesLedger = salesLedgerService.resolveSalesLedger(tenantId);
        Account toLedger = salesLedgerService.resolveCustomerLedger(tenantId, t.getToCustomer());

        Invoice inv = new Invoice();
        inv.setTenantId(tenantId);
        inv.setInvoiceNumber(generateInvoiceNumber(tenantId));
        inv.setInvoiceDate(date);
        inv.setCustomerId(toCustomerId);
        inv.setCustomerName(t.getToCustomer());
        inv.setSubtotal(toAmount);
        inv.setTotalAmount(toAmount);
        inv.setBalanceDue(toAmount);
        inv.setDcComments("Auto-created from dealer transfer " + t.getTransferNumber());

        if (toAmount.compareTo(BigDecimal.ZERO) > 0) {
            JournalEntry je = salesLedgerService.postEntry(toLedger, salesLedger, toAmount,
                    "DEALER_TRANSFER_INVOICE", t.getId(), inv.getInvoiceNumber(),
                    "Dealer transfer invoice " + inv.getInvoiceNumber() + " — " + t.getToCustomer(), date);
            inv.setPosted(true);
            inv.setJournalEntryId(je.getId());
            inv.setJournalEntryNumber(je.getEntryNumber());
            inv.setStatus(Invoice.InvoiceStatus.SENT);
            t.setToJournalEntryNumber(je.getEntryNumber());
        } else {
            inv.setStatus(Invoice.InvoiceStatus.DRAFT);
        }
        Invoice savedInv = invoiceRepository.save(inv);

        t.setToInvoiceId(savedInv.getId());
        t.setToInvoiceNumber(savedInv.getInvoiceNumber());
        t.setPosted(true);
        t.setStatus("POSTED");
        log.info("Dealer transfer {} posted: from-return {} / to-invoice {}",
                t.getTransferNumber(), t.getFromJournalEntryNumber(), savedInv.getInvoiceNumber());
        return repository.save(t);
    }

    private String generateInvoiceNumber(UUID tenantId) {
        long n = invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("INV-%d-%05d", LocalDate.now().getYear(), n + 1);
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
