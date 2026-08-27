package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateWarehouseRequest;
import com.erp.platform.modules.inventory.dto.StockItemDto;
import com.erp.platform.modules.inventory.dto.WarehouseDto;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.entity.StockMovement;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory and warehouse management")
public class InventoryController {

    private final StockService stockService;
    private final StockItemRepository stockItemRepository;
    private final StockLotRepository stockLotRepository;
    private final com.erp.platform.modules.master.repository.ProductRepository productRepository;
    private final TenantContext tenantContext;

    // ---- Warehouses ----

    @GetMapping("/api/v1/inventory/warehouses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List warehouses")
    public ResponseEntity<ApiResponse<PageResponse<WarehouseDto>>> listWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(stockService.listWarehouses(pageable)));
    }

    @GetMapping("/api/v1/inventory/warehouses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<ApiResponse<WarehouseDto>> getWarehouse(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getWarehouseById(id)));
    }

    @PostMapping("/api/v1/inventory/warehouses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create warehouse")
    public ResponseEntity<ApiResponse<WarehouseDto>> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(stockService.createWarehouse(request), "Warehouse created successfully"));
    }

    @PutMapping("/api/v1/inventory/warehouses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update warehouse")
    public ResponseEntity<ApiResponse<WarehouseDto>> updateWarehouse(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(stockService.updateWarehouse(id, request)));
    }

    @DeleteMapping("/api/v1/inventory/warehouses/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete warehouse (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable UUID id) {
        stockService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Warehouse deleted successfully"));
    }

    @GetMapping("/api/v1/inventory/warehouses/locations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List distinct warehouse locations for this tenant")
    public ResponseEntity<ApiResponse<List<String>>> listWarehouseLocations() {
        return ResponseEntity.ok(ApiResponse.success(stockService.listLocations()));
    }

    @GetMapping("/api/v1/inventory/warehouses/active-by-location")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active warehouses filtered by location")
    public ResponseEntity<ApiResponse<List<WarehouseDto>>> listWarehousesByLocation(
            @RequestParam String location) {
        return ResponseEntity.ok(ApiResponse.success(stockService.listWarehousesByLocation(location)));
    }

    // ---- Stock ----

    @GetMapping("/api/v1/inventory/stock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List stock items")
    public ResponseEntity<ApiResponse<PageResponse<StockItemDto>>> listStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID warehouseId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(ApiResponse.success(stockService.listStock(warehouseId, pageable)));
    }

    @GetMapping("/api/v1/inventory/stock/low")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get low stock items (quantity on hand <= 10)")
    public ResponseEntity<ApiResponse<java.util.List<StockItemDto>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(stockService.getLowStock()));
    }

    @PostMapping("/api/v1/inventory/stock/adjust")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adjust stock quantity")
    public ResponseEntity<ApiResponse<StockItemDto>> adjustStock(@RequestBody Map<String, Object> body) {
        UUID warehouseId = UUID.fromString(body.get("warehouseId").toString());
        UUID productId = UUID.fromString(body.get("productId").toString());
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        String reason = (String) body.getOrDefault("reason", "Manual adjustment");
        return ResponseEntity.ok(ApiResponse.success(
                stockService.adjust(warehouseId, productId, quantity, reason), "Stock adjusted"));
    }

    // ---- Barcode scan (mobile scanner) ----

    @GetMapping("/api/v1/inventory/scan/lookup")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Look up a product by scanned barcode / SKU / code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scanLookup(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success(stockService.scanLookup(code)));
    }

    @PostMapping("/api/v1/inventory/scan/receive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Receive scanned goods into a warehouse — adds quantity to on-hand and records a RECEIPT movement")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scanReceive(@RequestBody Map<String, Object> body) {
        String code = body.get("code") != null ? body.get("code").toString() : null;
        BigDecimal quantity = new BigDecimal(body.getOrDefault("quantity", "1").toString());
        UUID warehouseId = UUID.fromString(body.get("warehouseId").toString());
        String reference = body.get("reference") != null ? body.get("reference").toString() : null;
        return ResponseEntity.ok(ApiResponse.success(
                stockService.receiveByScan(code, quantity, warehouseId, reference), "Goods received"));
    }

    @PostMapping("/api/v1/inventory/stock/opening-balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk opening balance entry — creates a lot per entry (godown) or warehouse stock")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> openingBalance(
            @RequestBody List<Map<String, Object>> entries) {
        UUID tenantId = tenantContext.current();
        long existing = stockLotRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        int year = LocalDate.now().getYear();
        int seq = (int) existing;
        List<Map<String, Object>> results = new java.util.ArrayList<>();

        for (Map<String, Object> e : entries) {
            UUID productId = e.get("productId") != null ? UUID.fromString(e.get("productId").toString()) : null;
            BigDecimal qty = new BigDecimal(e.get("quantity").toString());
            String productName = str(e, "productName");
            String unit = str(e, "unit");
            String godownIdStr = str(e, "godownId");

            if (godownIdStr != null && !godownIdStr.isBlank()) {
                // Seed-industry flow: create a lot-based on-hand stock record (godown + net).
                seq++;
                StockLot lot = new StockLot();
                lot.setTenantId(tenantId);
                lot.setLotNo(String.format("OL-%d-%04d", year, seq));
                lot.setProductId(productId);
                lot.setProductName(productName);
                lot.setGodownId(UUID.fromString(godownIdStr));
                lot.setGodownName(str(e, "godownName"));
                String netId = str(e, "netId");
                if (netId != null && !netId.isBlank()) lot.setNetId(UUID.fromString(netId));
                lot.setNetName(str(e, "netName"));
                lot.setCropGroupName(str(e, "cropGroupName"));
                lot.setCropName(str(e, "cropName"));
                lot.setVarietyName(str(e, "varietyName"));
                lot.setMaterialGroupName(str(e, "materialGroupName"));
                lot.setMaterialItemName(str(e, "materialItemName"));
                lot.setMaterialType(str(e, "materialType"));
                lot.setMaterialState(str(e, "materialState"));
                String bags = str(e, "noOfBags");
                if (bags != null && !bags.isBlank()) {
                    try { lot.setNoOfBags(Integer.valueOf(bags.trim())); } catch (NumberFormatException ignore) {}
                }
                lot.setQuantity(qty);
                lot.setUnit(unit);
                lot.setSource("OPENING");
                // Opening stock has a value, and until now nothing captured it — every lot went in
                // cost-less, which is why stock reports valued the whole warehouse at nil. Take the
                // rate if one is entered, otherwise fall back to the product's purchase price.
                BigDecimal rate = decimal(e, "unitCost", "unitPrice", "rate");
                if (rate == null && productId != null) {
                    rate = productRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, productId)
                            .map(p -> p.getPurchasePrice())
                            .filter(p -> p != null && p.signum() > 0)
                            .orElse(null);
                }
                if (rate != null) lot.setUnitCost(rate);
                // Expiry drives the dispatch pick order, so it has to be captured where stock enters.
                lot.setExpiryDate(date(e, "expiryDate", "expiry"));
                lot.setManufactureDate(date(e, "manufactureDate", "productionDate"));
                lot.setExpired(lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(LocalDate.now()));
                StockLot saved = stockLotRepository.save(lot);

                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", saved.getId());
                m.put("lotNo", saved.getLotNo());
                m.put("productName", saved.getProductName());
                m.put("godownName", saved.getGodownName());
                m.put("netName", saved.getNetName());
                m.put("quantity", saved.getQuantity());
                results.add(m);
            } else if (e.get("warehouseId") != null) {
                // Generic/hospital flow: aggregate warehouse stock via the stock service.
                UUID warehouseId = UUID.fromString(e.get("warehouseId").toString());
                StockItemDto r = stockService.adjust(warehouseId, productId, qty, "Opening balance");
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("productName", r.getProductName() != null ? r.getProductName() : productName);
                m.put("quantity", r.getQuantityOnHand());
                results.add(m);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(results, "Opening balance set for " + results.size() + " items"));
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : null;
    }

    /** First parseable ISO date found under any of the given keys; null when none is supplied. */
    private static LocalDate date(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null || v.toString().isBlank()) continue;
            try {
                return LocalDate.parse(v.toString().trim().substring(0, 10));
            } catch (RuntimeException ignore) { /* try the next key */ }
        }
        return null;
    }

    /** First positive number found under any of the given keys — the form has used several names. */
    private static BigDecimal decimal(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null || v.toString().isBlank()) continue;
            try {
                BigDecimal d = new BigDecimal(v.toString().trim());
                if (d.signum() > 0) return d;
            } catch (NumberFormatException ignore) { /* try the next key */ }
        }
        return null;
    }

    // ---- Stock Ledger ----

    @GetMapping("/api/v1/inventory/stock-ledger")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Stock ledger — all movements for a product in a date range with opening/closing balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stockLedger(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getStockLedger(productId, warehouseId, from, to)));
    }

    // ---- Movements ----

    @GetMapping("/api/v1/inventory/movements")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List stock movements with optional filters: warehouseId, productId, type, dateFrom, dateTo")
    public ResponseEntity<ApiResponse<PageResponse<StockMovement>>> listMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "movementDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        StockMovement.MovementType typeEnum = (type != null && !type.isBlank())
                ? StockMovement.MovementType.valueOf(type) : null;
        LocalDate from = (dateFrom != null && !dateFrom.isBlank()) ? LocalDate.parse(dateFrom) : null;
        LocalDate to   = (dateTo   != null && !dateTo.isBlank())   ? LocalDate.parse(dateTo)   : null;
        return ResponseEntity.ok(ApiResponse.success(
                stockService.listMovements(warehouseId, productId, typeEnum, from, to, pageable)));
    }

    // ---- Stock Aging Report ----

    @GetMapping("/api/v1/inventory/stock-aging")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Stock aging report — groups stock items by product and age bucket based on receipt date")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> stockAging(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        UUID tenantId = tenantContext.current();
        LocalDate refDate = asOf != null ? asOf : LocalDate.now();

        var items = stockItemRepository.findByTenantIdAndDeletedAtIsNull(tenantId,
                PageRequest.of(0, 2000, Sort.by("productName"))).getContent();

        // Group by productId
        var byProduct = items.stream()
                .filter(i -> i.getQuantityOnHand() != null && i.getQuantityOnHand().compareTo(java.math.BigDecimal.ZERO) > 0)
                .collect(java.util.stream.Collectors.groupingBy(i -> i.getProductId().toString()));

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var entry : byProduct.entrySet()) {
            var stockItems = entry.getValue();
            var first = stockItems.get(0);

            java.math.BigDecimal totalQty = stockItems.stream()
                    .map(i -> i.getQuantityOnHand() != null ? i.getQuantityOnHand() : java.math.BigDecimal.ZERO)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            java.math.BigDecimal totalValue = stockItems.stream()
                    .map(i -> {
                        java.math.BigDecimal qty = i.getQuantityOnHand() != null ? i.getQuantityOnHand() : java.math.BigDecimal.ZERO;
                        java.math.BigDecimal cost = i.getAverageCost() != null ? i.getAverageCost() : java.math.BigDecimal.ZERO;
                        return qty.multiply(cost);
                    })
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            // Use createdAt as a proxy for receipt date; group all qty into one bucket per item
            Map<String, java.math.BigDecimal> buckets = new java.util.LinkedHashMap<>();
            buckets.put("0-30 days", java.math.BigDecimal.ZERO);
            buckets.put("31-60 days", java.math.BigDecimal.ZERO);
            buckets.put("61-90 days", java.math.BigDecimal.ZERO);
            buckets.put("91-180 days", java.math.BigDecimal.ZERO);
            buckets.put("180+ days", java.math.BigDecimal.ZERO);

            for (var si : stockItems) {
                java.math.BigDecimal qty = si.getQuantityOnHand() != null ? si.getQuantityOnHand() : java.math.BigDecimal.ZERO;
                if (qty.compareTo(java.math.BigDecimal.ZERO) <= 0) continue;
                LocalDate rcvDate = si.getCreatedAt() != null ? si.getCreatedAt().toLocalDate() : refDate;
                long days = java.time.temporal.ChronoUnit.DAYS.between(rcvDate, refDate);
                String bucket;
                if (days <= 30) bucket = "0-30 days";
                else if (days <= 60) bucket = "31-60 days";
                else if (days <= 90) bucket = "61-90 days";
                else if (days <= 180) bucket = "91-180 days";
                else bucket = "180+ days";
                buckets.merge(bucket, qty, java.math.BigDecimal::add);
            }

            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("productName", first.getProductName() != null ? first.getProductName() : "");
            row.put("category", first.getMaterialState() != null ? first.getMaterialState() : "");
            row.put("totalQty", totalQty);
            row.put("totalValue", totalValue);
            row.putAll(buckets);
            result.add(row);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
