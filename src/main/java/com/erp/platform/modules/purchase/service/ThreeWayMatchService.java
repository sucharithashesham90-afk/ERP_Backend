package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThreeWayMatchService {

    private final PurchaseOrderRepository purchaseOrderRepo;
    private final GoodsReceiptRepository goodsReceiptRepo;
    private final PurchaseInvoiceRepository purchaseInvoiceRepo;
    private final TenantContext tenantContext;

    public Map<String, Object> match(UUID purchaseOrderId) {
        UUID tenantId = tenantContext.current();

        PurchaseOrder po = purchaseOrderRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, purchaseOrderId)
                .orElseThrow(() -> AppException.notFound("Purchase order not found: " + purchaseOrderId));

        List<GoodsReceipt> grns = goodsReceiptRepo.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(tenantId, purchaseOrderId);
        List<PurchaseInvoice> invoices = purchaseInvoiceRepo.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(tenantId, purchaseOrderId);

        // Per-product: sum GRN accepted qty
        Map<UUID, BigDecimal> grnAcceptedByProduct = new HashMap<>();
        for (GoodsReceipt grn : grns) {
            for (GoodsReceiptItem item : grn.getItems()) {
                if (item.getProductId() == null) continue;
                BigDecimal accepted = item.getAcceptedQty() != null ? item.getAcceptedQty() : BigDecimal.ZERO;
                grnAcceptedByProduct.merge(item.getProductId(), accepted, BigDecimal::add);
            }
        }

        // Build line-level comparison
        List<Map<String, Object>> lines = new ArrayList<>();
        BigDecimal poTotalValue = BigDecimal.ZERO;
        BigDecimal grnTotalValue = BigDecimal.ZERO;

        for (PurchaseOrderItem poItem : po.getItems()) {
            UUID productId = poItem.getProductId();
            BigDecimal poQty = poItem.getQuantity() != null ? poItem.getQuantity() : BigDecimal.ZERO;
            BigDecimal unitPrice = poItem.getUnitPrice() != null ? poItem.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal grnQty = grnAcceptedByProduct.getOrDefault(productId, BigDecimal.ZERO);

            BigDecimal poValue = poQty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grnValue = grnQty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal qtyVariance = grnQty.subtract(poQty);
            BigDecimal valueVariance = grnValue.subtract(poValue);

            Map<String, Object> line = new HashMap<>();
            line.put("productId", productId);
            line.put("productName", poItem.getProductName());
            line.put("poQty", poQty);
            line.put("grnAcceptedQty", grnQty);
            line.put("unitPrice", unitPrice);
            line.put("poValue", poValue);
            line.put("grnValue", grnValue);
            line.put("qtyVariance", qtyVariance);
            line.put("valueVariance", valueVariance);
            line.put("matched", qtyVariance.compareTo(BigDecimal.ZERO) == 0);
            lines.add(line);

            poTotalValue = poTotalValue.add(poValue);
            grnTotalValue = grnTotalValue.add(grnValue);
        }

        // ── Amount reconciliation: compare like-for-like across PO and Invoice ──
        // Three components must each match: taxable base amount, tax amount, and the
        // grand total ("sales value"). Comparing the invoice grand total (incl. tax)
        // against a tax-exclusive PO value would always report a false variance.
        BigDecimal poBaseAmount  = nz(po.getSubtotal());
        BigDecimal poTaxAmount   = nz(po.getTaxAmount());
        BigDecimal poGrandTotal  = nz(po.getTotalAmount());

        // Fall back to line-derived values when the PO header amounts were never set.
        if (poBaseAmount.signum() == 0 && poGrandTotal.signum() == 0) {
            BigDecimal lineBase = BigDecimal.ZERO;
            BigDecimal lineTax  = BigDecimal.ZERO;
            for (PurchaseOrderItem poItem : po.getItems()) {
                BigDecimal qty   = nz(poItem.getQuantity());
                BigDecimal price = nz(poItem.getUnitPrice());
                lineBase = lineBase.add(qty.multiply(price)).subtract(nz(poItem.getDiscountAmount()));
                lineTax  = lineTax.add(nz(poItem.getTaxAmount()));
            }
            poBaseAmount = lineBase;
            poTaxAmount  = lineTax;
            poGrandTotal = lineBase.add(lineTax);
        }

        BigDecimal invBaseAmount = sum(invoices, PurchaseInvoice::getSubtotal);
        BigDecimal invTaxAmount  = sum(invoices, PurchaseInvoice::getTaxAmount);
        BigDecimal invGrandTotal = sum(invoices, PurchaseInvoice::getTotalAmount);

        // ── Proration for partial receipts ──
        // The vendor bills for what was actually received, so scale the PO's amounts
        // down to the received (value-weighted) fraction before comparing. When fully
        // received — or when no GRN exists yet — the ratio is 1 and this is a no-op.
        BigDecimal receivedRatio = BigDecimal.ONE;
        if (!grns.isEmpty() && poTotalValue.signum() != 0) {
            receivedRatio = grnTotalValue.divide(poTotalValue, 6, RoundingMode.HALF_UP);
        }
        boolean partialReceipt = receivedRatio.compareTo(BigDecimal.ONE) != 0;

        BigDecimal expectedBase  = poBaseAmount.multiply(receivedRatio);
        BigDecimal expectedTax   = poTaxAmount.multiply(receivedRatio);
        BigDecimal expectedTotal = poGrandTotal.multiply(receivedRatio);

        Map<String, Object> baseMatch  = component(poBaseAmount, expectedBase, invBaseAmount);
        Map<String, Object> taxMatch   = component(poTaxAmount, expectedTax, invTaxAmount);
        Map<String, Object> totalMatch = component(poGrandTotal, expectedTotal, invGrandTotal);

        boolean baseMatched  = (boolean) baseMatch.get("matched");
        boolean taxMatched   = (boolean) taxMatch.get("matched");
        boolean totalMatched = (boolean) totalMatch.get("matched");

        boolean linesMatched = lines.stream().allMatch(l -> Boolean.TRUE.equals(l.get("matched")));
        // A PO can legitimately have no invoice yet; only enforce amount matching once billed.
        boolean amountsMatched = invoices.isEmpty() || (baseMatched && taxMatched && totalMatched);
        boolean threeWayMatched = linesMatched && amountsMatched;

        BigDecimal invoiceVsPoVariance = invGrandTotal.subtract(poGrandTotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal invoiceVsGrnVariance = invGrandTotal.subtract(grnTotalValue).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("purchaseOrderId", po.getId());
        result.put("poNumber", po.getPoNumber());
        result.put("vendorName", po.getVendorName());
        result.put("poTotalValue", poTotalValue.setScale(2, RoundingMode.HALF_UP)); // line base (qty × price)
        result.put("grnTotalValue", grnTotalValue.setScale(2, RoundingMode.HALF_UP));
        result.put("invoiceTotalAmount", invGrandTotal.setScale(2, RoundingMode.HALF_UP));
        result.put("invoiceVsPoVariance", invoiceVsPoVariance);
        result.put("invoiceVsGrnVariance", invoiceVsGrnVariance);
        // Component-wise amount reconciliation (PO amount, tax amount, sales value)
        result.put("baseAmount", baseMatch);   // taxable / purchase order amount
        result.put("taxAmount", taxMatch);     // tax amount
        result.put("salesValue", totalMatch);  // grand total (base + tax)
        result.put("baseMatched", baseMatched);
        result.put("taxMatched", taxMatched);
        result.put("totalMatched", totalMatched);
        result.put("receivedRatio", receivedRatio);
        result.put("partialReceipt", partialReceipt);
        result.put("grnCount", grns.size());
        result.put("invoiceCount", invoices.size());
        result.put("threeWayMatched", threeWayMatched);
        result.put("lines", lines);
        return result;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal sum(List<PurchaseInvoice> invoices, java.util.function.Function<PurchaseInvoice, BigDecimal> f) {
        return invoices.stream().map(i -> nz(f.apply(i))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Builds a {po, expected, invoice, variance, matched} comparison row with a ₹0.01
     * tolerance. The invoice is matched against {@code expected} (the PO amount prorated
     * to the received quantity); {@code po} carries the full-order figure for reference.
     */
    private static Map<String, Object> component(BigDecimal poValue, BigDecimal expectedValue, BigDecimal invoiceValue) {
        BigDecimal po = nz(poValue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = nz(expectedValue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal inv = nz(invoiceValue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal variance = inv.subtract(expected).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> row = new HashMap<>();
        row.put("po", po);
        row.put("expected", expected);
        row.put("invoice", inv);
        row.put("variance", variance);
        row.put("matched", variance.abs().compareTo(new BigDecimal("0.01")) <= 0);
        return row;
    }
}
