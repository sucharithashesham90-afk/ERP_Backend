package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Lot-based stock ({@link StockLot}) — listing for pickers and the Stock Movement transfer
 * (move a quantity of a lot from one godown/net to another within the location).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory/stock-lots")
@RequiredArgsConstructor
@Tag(name = "Inventory - Stock Lots", description = "Lot stock listing and godown/net movement")
public class StockLotController {

    private final StockLotRepository stockLotRepository;
    /** A purchase invoice keeps its lines as JSON; the lot search reads them to match goods. */
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    /** Falls back to the product's purchase price when neither invoice nor lot carries one. */
    private final com.erp.platform.modules.master.repository.ProductRepository productRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List stock lots, optionally filtered by godown (for movement/picker)")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) UUID godownId) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockLot> lots = (godownId != null)
                ? stockLotRepository.findByTenantIdAndGodownIdAndDeletedAtIsNull(tenantId, godownId, pageable)
                : stockLotRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(lots.map(this::toRow))));
    }

    /**
     * Lots that match every filter the caller gives — the search behind the Lot No. field on a
     * purchase return.
     *
     * <p>Filters narrow rather than widen: a lot has to satisfy all of them. That is the point.
     * Offering an operator the whole lot register and trusting them to pick the right one is how a
     * return ends up booked against the wrong lot, and a lot's identity is the whole of its value.
     *
     * <p>Product is optional by design: not every lot is tied to a SKU, so when it is absent the
     * search narrows only as far as variety rather than returning nothing.
     *
     * <p>The purchase invoice is matched through the goods on it, since a lot carries no invoice
     * reference — only lots of a product, variety or crop that appears on that invoice are offered.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search lots by crop group, variety, product, purchase invoice and location")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> search(
            @RequestParam(required = false) String cropGroupName,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String varietyName,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID invoiceId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) UUID godownId,
            @RequestParam(defaultValue = "200") int limit) {

        UUID tenantId = tenantContext.current();
        Set<String> invoiceNames = invoiceId == null ? null : namesOnInvoice(tenantId, invoiceId);

        List<Map<String, Object>> rows = stockLotRepository
                .findByTenantIdAndDeletedAtIsNull(tenantId, Pageable.unpaged())
                .stream()
                // Nothing left in the lot is nothing to send back.
                .filter(l -> l.getQuantity() != null && l.getQuantity().signum() > 0)
                .filter(l -> matches(l.getCropGroupName(), cropGroupName))
                .filter(l -> matches(l.getCropName(), cropName))
                .filter(l -> matches(l.getVarietyName(), varietyName))
                .filter(l -> productId == null || productId.equals(l.getProductId()))
                .filter(l -> godownId == null || godownId.equals(l.getGodownId()))
                .filter(l -> matches(l.getLocation(), location) || matches(l.getGodownName(), location))
                .filter(l -> invoiceNames == null || onInvoice(l, invoiceNames))
                .limit(Math.max(1, limit))
                .map(l -> {
                    Map<String, Object> row = toRow(l);
                    // What this lot was bought for, so the return line can be priced without the
                    // operator retyping a figure the purchase already agreed. Resolved here rather
                    // than in the browser so the number shown is the same one the server would post.
                    row.put("purchasePrice", purchasePriceFor(tenantId, l, invoiceId));
                    return row;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    /**
     * The price to bill a supplier back at: what they invoiced, else what the lot cost, else the
     * product's purchase price. Null when nothing upstream knows — the operator then has to say.
     */
    private BigDecimal purchasePriceFor(UUID tenantId, StockLot lot, UUID invoiceId) {
        if (invoiceId != null) {
            BigDecimal fromInvoice = invoiceLinePrice(tenantId, invoiceId, lot);
            if (fromInvoice != null && fromInvoice.signum() > 0) return fromInvoice;
        }
        if (lot.getUnitCost() != null && lot.getUnitCost().signum() > 0) return lot.getUnitCost();
        if (lot.getProductId() != null) {
            return productRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, lot.getProductId())
                    .map(p -> p.getPurchasePrice())
                    .filter(p -> p != null && p.signum() > 0)
                    .orElse(null);
        }
        return null;
    }

    /** The rate on the invoice line matching this lot's product, variety or crop. */
    private BigDecimal invoiceLinePrice(UUID tenantId, UUID invoiceId, StockLot lot) {
        return purchaseInvoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, invoiceId)
                .map(inv -> {
                    if (inv.getItemsJson() == null || inv.getItemsJson().isBlank()) return null;
                    try {
                        for (JsonNode line : new ObjectMapper().readTree(inv.getItemsJson())) {
                            if (!lineMatchesLot(line, lot)) continue;
                            JsonNode price = line.get("unitPrice");
                            if (price != null && !price.asText().isBlank()) {
                                return new BigDecimal(price.asText().trim());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Could not price lot {} from invoice {}: {}",
                                lot.getLotNo(), invoiceId, e.getMessage());
                    }
                    return (BigDecimal) null;
                })
                .orElse(null);
    }

    private static boolean lineMatchesLot(JsonNode line, StockLot lot) {
        for (String key : new String[]{"productName", "varietyName", "cropName", "itemName"}) {
            JsonNode v = line.get(key);
            if (v == null || v.asText().isBlank()) continue;
            String name = v.asText().trim();
            if (name.equalsIgnoreCase(lot.getProductName())
                    || name.equalsIgnoreCase(lot.getVarietyName())
                    || name.equalsIgnoreCase(lot.getCropName())) {
                return true;
            }
        }
        return false;
    }

    /** Case-insensitive, and a blank filter matches everything rather than nothing. */
    private static boolean matches(String value, String filter) {
        if (filter == null || filter.isBlank()) return true;
        return value != null && value.equalsIgnoreCase(filter.trim());
    }

    private static boolean onInvoice(StockLot lot, Set<String> names) {
        return Stream.of(lot.getProductName(), lot.getVarietyName(), lot.getCropName())
                .filter(s -> s != null && !s.isBlank())
                .anyMatch(s -> names.contains(s.trim().toLowerCase()));
    }

    /** Product, variety and crop names appearing on a purchase invoice's stored lines. */
    private Set<String> namesOnInvoice(UUID tenantId, UUID invoiceId) {
        Set<String> names = new HashSet<>();
        purchaseInvoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, invoiceId)
                .ifPresent(inv -> {
                    if (inv.getItemsJson() == null || inv.getItemsJson().isBlank()) return;
                    try {
                        for (JsonNode line : new ObjectMapper().readTree(inv.getItemsJson())) {
                            for (String key : new String[]{"productName", "varietyName", "cropName", "itemName"}) {
                                JsonNode v = line.get(key);
                                if (v != null && !v.asText().isBlank()) names.add(v.asText().trim().toLowerCase());
                            }
                        }
                    } catch (Exception e) {
                        // A malformed line list must not make the search return nothing at all;
                        // fall back to not filtering by invoice.
                        log.warn("Could not read invoice {} lines for lot search: {}", invoiceId, e.getMessage());
                    }
                });
        return names.isEmpty() ? null : names;
    }

    @PostMapping("/move")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Move a quantity of a lot from one godown/net to another")
    public ResponseEntity<ApiResponse<Map<String, Object>>> move(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String lotId = str(req, "stockLotId");
        if (lotId == null) return bad("stockLotId is required");
        String toGodownId = str(req, "toGodownId");
        if (toGodownId == null) return bad("Destination godown is required");
        BigDecimal moveQty = decimal(req, "quantity");
        if (moveQty == null || moveQty.compareTo(BigDecimal.ZERO) <= 0) return bad("A positive quantity is required");

        StockLot source = stockLotRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, UUID.fromString(lotId))
                .orElse(null);
        if (source == null) return bad("Source lot not found");

        BigDecimal srcQty = source.getQuantity() != null ? source.getQuantity() : BigDecimal.ZERO;
        if (moveQty.compareTo(srcQty) > 0) return bad("Cannot move more than the available quantity (" + srcQty + ")");

        UUID toGod = UUID.fromString(toGodownId);
        String toGodName = str(req, "toGodownName");
        String toNetIdStr = str(req, "toNetId");
        UUID toNet = (toNetIdStr != null && !toNetIdStr.isBlank()) ? UUID.fromString(toNetIdStr) : null;
        String toNetName = str(req, "toNetName");

        StockLot result;
        if (moveQty.compareTo(srcQty) == 0) {
            // Full move — relocate the lot to the destination godown/net.
            source.setGodownId(toGod);
            source.setGodownName(toGodName);
            source.setNetId(toNet);
            source.setNetName(toNetName);
            result = stockLotRepository.save(source);
        } else {
            // Partial move — reduce the source lot and create a destination lot for the moved qty.
            source.setQuantity(srcQty.subtract(moveQty));
            stockLotRepository.save(source);

            StockLot dest = new StockLot();
            dest.setTenantId(tenantId);
            dest.setLotNo(source.getLotNo());
            dest.setProductId(source.getProductId());
            dest.setProductName(source.getProductName());
            dest.setCropGroupName(source.getCropGroupName());
            dest.setCropName(source.getCropName());
            dest.setVarietyName(source.getVarietyName());
            dest.setMaterialType(source.getMaterialType());
            dest.setMaterialState(source.getMaterialState());
            dest.setGodownId(toGod);
            dest.setGodownName(toGodName);
            dest.setNetId(toNet);
            dest.setNetName(toNetName);
            dest.setQuantity(moveQty);
            dest.setUnit(source.getUnit());
            dest.setSource("MOVE");
            result = stockLotRepository.save(dest);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lotNo", result.getLotNo());
        m.put("movedQuantity", moveQty);
        m.put("toGodownName", toGodName);
        m.put("toNetName", toNetName);
        m.put("at", LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(m, "Stock moved"));
    }

    private Map<String, Object> toRow(StockLot l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("lotNo", l.getLotNo());
        String item = l.getProductName() != null ? l.getProductName()
                : (l.getVarietyName() != null ? l.getVarietyName() : l.getCropName());
        m.put("item", item);
        m.put("productName", l.getProductName());
        m.put("cropGroupName", l.getCropGroupName());
        m.put("cropName", l.getCropName());
        m.put("varietyName", l.getVarietyName());
        m.put("godownId", l.getGodownId());
        m.put("godownName", l.getGodownName());
        m.put("netId", l.getNetId());
        m.put("netName", l.getNetName());
        m.put("noOfBags", l.getNoOfBags());
        m.put("quantity", l.getQuantity());
        m.put("unit", l.getUnit());
        // Exposed so the dispatch lot picker can filter by material state (e.g. "Packing") and location.
        m.put("materialState", l.getMaterialState());
        m.put("materialType", l.getMaterialType());
        m.put("location", l.getLocation());
        return m;
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> bad(String msg) {
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", msg));
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : null;
    }

    private static BigDecimal decimal(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
