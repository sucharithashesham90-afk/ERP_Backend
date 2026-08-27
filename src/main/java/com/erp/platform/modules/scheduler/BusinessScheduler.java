package com.erp.platform.modules.scheduler;

import com.erp.platform.modules.inventory.entity.StockItem;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.repository.ProductRepository;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessScheduler {

    private final InvoiceRepository invoiceRepository;
    private final StockItemRepository stockItemRepository;
    private final ProductRepository productRepository;

    /**
     * Runs every day at 01:00 AM — marks SENT/PARTIALLY_PAID invoices as OVERDUE
     * if their due date has passed.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();
        List<Invoice> candidates = invoiceRepository.findInvoicesDueForOverdue(today);
        if (candidates.isEmpty()) return;

        for (Invoice inv : candidates) {
            inv.setStatus(InvoiceStatus.OVERDUE);
        }
        invoiceRepository.saveAll(candidates);
        log.info("Marked {} invoice(s) as OVERDUE", candidates.size());
    }

    /**
     * Runs every day at 06:00 AM — logs low-stock alerts for items below reorder level.
     * This creates an audit trail; a notification system can consume these logs.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void checkLowStockAlerts() {
        List<StockItem> allItems = stockItemRepository.findAll();
        int alertCount = 0;

        for (StockItem item : allItems) {
            BigDecimal qty = item.getQuantityOnHand() != null ? item.getQuantityOnHand() : BigDecimal.ZERO;

            // Look up reorder level from Product master
            BigDecimal reorderLevel = BigDecimal.ZERO;
            if (item.getProductId() != null) {
                reorderLevel = productRepository.findById(item.getProductId())
                        .map(Product::getReorderLevel)
                        .filter(r -> r != null)
                        .orElse(BigDecimal.ZERO);
            }

            if (reorderLevel.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(reorderLevel) <= 0) {
                log.warn("LOW STOCK ALERT: product='{}' warehouse='{}' qty={} reorderLevel={}",
                        item.getProductName(), item.getWarehouseName(), qty, reorderLevel);
                alertCount++;
            }
        }

        if (alertCount > 0) {
            log.info("Low-stock check complete: {} item(s) below reorder level", alertCount);
        }
    }
}
