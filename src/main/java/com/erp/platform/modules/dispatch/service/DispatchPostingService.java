package com.erp.platform.modules.dispatch.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.dispatch.entity.DispatchChallan;
import com.erp.platform.modules.dispatch.entity.DispatchChallanLine;
import com.erp.platform.modules.dispatch.repository.DispatchChallanRepository;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.InvoiceItem;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.service.SalesInvoicePostingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Dispatches a challan: reduces on-hand stock for the challan's lot (both the lot-based StockLot and
 * the aggregate StockItem, so reports stay in sync) and auto-creates a sales invoice with the challan's
 * line items, then posts it to the ledgers (customer debited, sales credited) so the sale shows up in
 * the customer's ledger straight away. Guarded so a challan is dispatched once.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchPostingService {

    private final DispatchChallanRepository challanRepository;
    private final StockLotRepository stockLotRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockService stockService;
    private final SalesInvoicePostingService salesInvoicePostingService;
    private final TenantContext tenantContext;

    @Transactional
    public DispatchChallan dispatch(UUID challanId) {
        UUID tenantId = tenantContext.current();
        DispatchChallan c = challanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, challanId)
                .orElseThrow(() -> AppException.notFound("Dispatch challan not found: " + challanId));
        if (c.getInvoiceId() != null)
            throw AppException.badRequest("Challan is already dispatched (invoice " + c.getInvoiceNumber() + ")");

        // 1) Reduce stock — for each line item when present, else the single header lot.
        BigDecimal value;
        List<DispatchChallanLine> lines = c.getItems();
        if (lines != null && !lines.isEmpty()) {
            for (DispatchChallanLine line : lines)
                reduceLotStock(tenantId, line.getLotNumber(), line.getQuantity(), c);
            value = lines.stream()
                    .map(l -> l.getValue() == null ? BigDecimal.ZERO : l.getValue())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (value.compareTo(BigDecimal.ZERO) == 0 && c.getValue() != null)
                value = c.getValue();
        } else {
            reduceLotStock(tenantId, c.getLotNumber(), c.getQuantityKgs(), c);
            value = c.getValue() == null ? BigDecimal.ZERO : c.getValue();
        }

        // 2) Auto-create the sales invoice (DRAFT) with the challan's line items.
        Invoice inv = new Invoice();
        inv.setTenantId(tenantId);
        inv.setInvoiceNumber(generateInvoiceNumber(tenantId));
        inv.setInvoiceDate(c.getChallanDate() != null ? c.getChallanDate() : LocalDate.now());
        inv.setStatus(Invoice.InvoiceStatus.DRAFT);
        inv.setCustomerId(parseUuid(c.getCustomerId()));
        inv.setCustomerName(c.getCustomerName());
        inv.setSalesArea(c.getSalesArea());
        inv.setSubtotal(value);
        inv.setTotalAmount(value);
        inv.setBalanceDue(value);
        inv.setDcComments("Auto-created from dispatch challan " + c.getChallanNumber());
        inv.setDispatchChallanNumber(c.getChallanNumber());

        // Carry the dispatch location and logistics context onto the invoice.
        inv.setFromLocation(c.getDispatchLocation());
        inv.setLorryNumber(firstNonBlank(c.getLorryNo(), c.getVehicleNumber()));
        inv.setWayBillNumber(c.getWayBillNo());
        inv.setRrRlNumber(c.getRrRlNo());
        inv.setCarrier(firstNonBlank(c.getCarrier(), c.getFreightCarrier()));

        if (lines != null && !lines.isEmpty()) {
            for (DispatchChallanLine line : lines) {
                InvoiceItem item = buildItem(inv, line.getProductId(), lineProductName(line),
                        buildLineDescription(line), line.getQuantity(), line.getRate(), line.getValue());
                item.setPackType(line.getPacking());
                inv.getItems().add(item);
            }
        } else {
            inv.getItems().add(buildItem(inv, null, c.getProductName(),
                    isNotBlank(c.getLotNumber()) ? "Lot: " + c.getLotNumber() : null,
                    c.getQuantityKgs(), null, value));
        }
        Invoice savedInv = invoiceRepository.save(inv);

        // Auto-complete and post the invoice so the sales voucher is created and reflects in Voucher Search and Ledger Search
        try {
            salesInvoicePostingService.completeInvoice(savedInv.getId());
        } catch (Exception e) {
            log.warn("Auto-posting sales invoice {} on dispatch skipped: {}", savedInv.getInvoiceNumber(), e.getMessage());
        }

        // 3) Link the challan to its invoice; keep DELIVERED if already set, else mark DISPATCHED.
        c.setInvoiceId(savedInv.getId());
        c.setInvoiceNumber(savedInv.getInvoiceNumber());
        if (c.getStatus() == null || c.getStatus().isBlank() || "DRAFT".equalsIgnoreCase(c.getStatus()))
            c.setStatus("DISPATCHED");
        log.info("Challan {} posted ({}): {} line(s), invoice {} created with {} item(s)",
                c.getChallanNumber(), c.getStatus(), (lines != null ? lines.size() : 0),
                savedInv.getInvoiceNumber(), savedInv.getItems().size());
        return challanRepository.save(c);
    }

    /** Build an invoice line item from a dispatch line. */
    private InvoiceItem buildItem(Invoice inv, String productId, String productName,
                                  String description, BigDecimal qty, BigDecimal rate, BigDecimal value) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(inv);
        item.setProductId(parseUuid(productId));
        item.setProductName(productName);
        item.setDescription(description);
        item.setQuantity(qty != null ? qty : BigDecimal.ONE);
        item.setUnitPrice(rate != null ? rate : BigDecimal.ZERO);
        item.setTotalAmount(value != null ? value : BigDecimal.ZERO);
        return item;
    }

    /** Best product name for a dispatch line: product name, else crop-variety label, else crop + variety. */
    private String lineProductName(DispatchChallanLine line) {
        String name = firstNonBlank(line.getProductName(), line.getCropVariety());
        if (isNotBlank(name)) return name;
        String cv = (nz(line.getCrop()) + " " + nz(line.getVariety())).trim();
        return isNotBlank(cv) ? cv : null;
    }

    /** Full product detail carried onto the invoice line: crop group / crop / variety / lot. */
    private String buildLineDescription(DispatchChallanLine line) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, "Crop Group", line.getCropGroup());
        appendPart(sb, "Crop", line.getCrop());
        appendPart(sb, "Variety", line.getVariety());
        appendPart(sb, "Lot", line.getLotNumber());
        return sb.length() == 0 ? null : sb.toString();
    }

    private void appendPart(StringBuilder sb, String label, String value) {
        if (isNotBlank(value)) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(label).append(": ").append(value.trim());
        }
    }

    private static boolean isNotBlank(String s) { return s != null && !s.isBlank(); }
    private static String nz(String s) { return s == null ? "" : s; }

    /**
     * Reduce on-hand stock for a lot; no-op when lot/qty are missing.
     * Reduces the lot-based StockLot AND (best-effort) the aggregate StockItem so inventory
     * reports/screens reflect the dispatch. The aggregate sync is wrapped so a missing StockItem
     * (lot-only stock) or godown↔warehouse mismatch doesn't fail the dispatch.
     */
    private void reduceLotStock(UUID tenantId, String lotNumber, BigDecimal qty, DispatchChallan c) {
        BigDecimal q = qty == null ? BigDecimal.ZERO : qty;
        if (lotNumber == null || lotNumber.isBlank() || q.compareTo(BigDecimal.ZERO) <= 0) return;
        List<StockLot> lots = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber);
        if (lots.isEmpty()) {
            log.warn("No stock lot found for lot number: {} during dispatch {}; proceeding with auto invoice", lotNumber, c.getChallanNumber());
            return;
        }
        // Expired stock must not ship. Where it is all that carries this lot number the dispatch is
        // stopped rather than quietly fulfilled — shipping expired seed is worse than shipping late.
        java.time.LocalDate today = java.time.LocalDate.now();
        List<StockLot> usable = lots.stream()
                .filter(l -> !isExpired(l, today))
                .toList();
        if (usable.isEmpty()) {
            throw com.erp.platform.common.exception.AppException.badRequest(
                    "Lot " + lotNumber + " has expired and cannot be dispatched");
        }

        // Among the lots carrying this number, take the one expiring soonest that can cover the
        // quantity — first-expiry-first-out, so stock nearing its date leaves before newer stock.
        // Undated lots sort last: nothing is known to be perishing, so they can wait.
        java.util.Comparator<StockLot> byExpiry = java.util.Comparator.comparing(
                StockLot::getExpiryDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        StockLot lot = usable.stream()
                .filter(l -> l.getQuantity() != null && l.getQuantity().compareTo(q) >= 0)
                .min(byExpiry)
                .orElseGet(() -> usable.stream().min(byExpiry).orElse(usable.get(0)));
        BigDecimal available = lot.getQuantity() == null ? BigDecimal.ZERO : lot.getQuantity();
        BigDecimal newQty = available.subtract(q);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Lot {} stock ({}) lower than required dispatch qty ({}); setting to 0", lotNumber, available, q);
            newQty = BigDecimal.ZERO;
        }
        lot.setQuantity(newQty);
        stockLotRepository.save(lot);

        // Keep the aggregate StockItem in sync (records a movement + COGS). Best-effort.
        if (lot.getProductId() != null && lot.getGodownId() != null) {
            try {
                stockService.deductStock(lot.getProductId(), lot.getGodownId(), q,
                        "DISPATCH_CHALLAN", c.getId(), c.getChallanNumber());
            } catch (Exception ex) {
                log.warn("Aggregate stock not synced for lot {} (product {}): {}",
                        lotNumber, lot.getProductId(), ex.getMessage());
            }
        }
    }

    /** Past its expiry date, or already flagged expired by the nightly sweep. */
    private static boolean isExpired(StockLot lot, java.time.LocalDate today) {
        if (Boolean.TRUE.equals(lot.getExpired())) return true;
        return lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today);
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private String generateInvoiceNumber(UUID tenantId) {
        long n = invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("INV-%d-%05d", LocalDate.now().getYear(), n + 1);
    }

    private UUID parseUuid(String s) {
        try {
            return (s == null || s.isBlank()) ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
