package com.erp.platform.modules.sales.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * One-off backfill for invoices that were created (typically from dispatch challans) before sales
 * invoices auto-posted to the ledger, so they sit unposted with no journal entry and never appear in
 * the customer ledger / voucher search. Posts each one via {@link SalesInvoicePostingService}.
 *
 * <p>Deliberately NOT {@code @Transactional}: each {@code completeInvoice} call goes through the Spring
 * proxy and commits in its own transaction, so a single bad invoice fails in isolation instead of
 * rolling back the whole batch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePostingBackfillService {

    private final InvoiceRepository invoiceRepository;
    private final SalesInvoicePostingService postingService;
    private final TenantContext tenantContext;

    /** Post every unposted invoice for the current tenant. Returns {found, posted, failed}. */
    public Map<String, Integer> backfillUnposted() {
        List<Invoice> unposted = invoiceRepository.findUnpostedForBackfill(tenantContext.current());
        int posted = 0, failed = 0;
        for (Invoice inv : unposted) {
            try {
                postingService.completeInvoice(inv.getId());
                posted++;
            } catch (Exception e) {
                failed++;
                log.warn("Backfill: could not post invoice {}: {}", inv.getInvoiceNumber(), e.getMessage());
            }
        }
        log.info("Invoice posting backfill: {} found, {} posted, {} failed", unposted.size(), posted, failed);
        return Map.of("found", unposted.size(), "posted", posted, "failed", failed);
    }
}
