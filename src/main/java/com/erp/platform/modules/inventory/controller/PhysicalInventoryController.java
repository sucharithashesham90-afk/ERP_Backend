package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.inventory.entity.PhysicalInventoryAdjustment;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.PhysicalInventoryAdjustmentRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/physical-inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory - Physical Inventory", description = "Physical count and adjustments")
public class PhysicalInventoryController {

    private final PhysicalInventoryAdjustmentRepository repo;
    private final StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;

    /**
     * Physical stock listing, sourced from the lot-based stock ledger ({@link StockLot}).
     * Opening Stock and lot-wise intake write lots here, so they show up as available stock.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List physical (lot-based) stock, optionally filtered by godown")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID godownId,
            @RequestParam(required = false) String cropGroupName,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String varietyName,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) String materialState) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockLot> lots = stockLotRepository.search(tenantId, godownId,
                blankToNull(cropGroupName), blankToNull(cropName), blankToNull(varietyName),
                blankToNull(materialType), blankToNull(materialState), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(lots.map(this::toRow))));
    }

    /**
     * Lot detail for the Process Job screen: give a lot number, get its crop group / crop / variety /
     * seed state / godown / location and the available quantity (summed across matching lot rows).
     */
    @GetMapping("/by-lot/{lotNo}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a lot's stock detail by lot number (for Process Job auto-populate)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> byLot(@PathVariable String lotNo) {
        UUID tenantId = tenantContext.current();
        var lots = stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNo);
        if (lots.isEmpty()) return ResponseEntity.ok(ApiResponse.success(null));

        // Merge across every row that shares this lot number so no descriptive field is lost (any
        // single row can be sparse), and total the bags / quantity — a scan should surface the
        // complete physical-inventory record for the lot.
        Map<String, Object> m = toRow(lots.get(0));
        BigDecimal totalQty = BigDecimal.ZERO;
        int totalBags = 0;
        for (StockLot l : lots) {
            coalesce(m, "materialName", l.getProductName());
            coalesce(m, "productName", l.getProductName());
            coalesce(m, "cropGroupName", l.getCropGroupName());
            coalesce(m, "cropName", l.getCropName());
            coalesce(m, "varietyName", l.getVarietyName());
            coalesce(m, "materialTypeName", l.getMaterialType());
            coalesce(m, "materialStateName", l.getMaterialState());
            coalesce(m, "location", l.getLocation());
            coalesce(m, "godownName", l.getGodownName());
            coalesce(m, "netName", l.getNetName());
            coalesce(m, "unit", l.getUnit());
            coalesce(m, "source", l.getSource());
            if (l.getQuantity() != null) totalQty = totalQty.add(l.getQuantity());
            if (l.getNoOfBags() != null) totalBags += l.getNoOfBags();
        }
        m.put("quantity", totalQty);
        m.put("noOfBags", totalBags);
        m.put("availableQuantity", totalQty);
        return ResponseEntity.ok(ApiResponse.success(m));
    }

    /** Fill a map key only when it is currently empty and the candidate value is non-empty. */
    private static void coalesce(Map<String, Object> m, String key, Object value) {
        Object current = m.get(key);
        if ((current == null || "".equals(current)) && value != null && !"".equals(value)) {
            m.put(key, value);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private Map<String, Object> toRow(StockLot l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("lotNo", l.getLotNo());
        m.put("materialName", l.getProductName());
        m.put("productName", l.getProductName());
        m.put("cropGroupName", l.getCropGroupName());
        m.put("cropName", l.getCropName());
        m.put("varietyName", l.getVarietyName());
        m.put("materialTypeName", l.getMaterialType());
        m.put("materialStateName", l.getMaterialState());
        m.put("location", l.getLocation());
        m.put("godownName", l.getGodownName());
        m.put("netName", l.getNetName());
        m.put("noOfBags", l.getNoOfBags());
        m.put("quantity", l.getQuantity());
        m.put("unit", l.getUnit());
        m.put("source", l.getSource());
        return m;
    }

    @PostMapping("/adjust")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Record a physical inventory adjustment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adjust(@RequestBody Map<String, Object> req) {
        String stockId = PayloadUtils.str(req, "stockId");
        BigDecimal adjustment = PayloadUtils.decimal(req, "adjustment");
        if (stockId == null) throw AppException.badRequest("stockId is required");
        if (adjustment == null || adjustment.compareTo(BigDecimal.ZERO) == 0)
            throw AppException.badRequest("A non-zero adjustment value is required");

        PhysicalInventoryAdjustment e = new PhysicalInventoryAdjustment();
        e.setTenantId(tenantContext.current());
        e.setStockId(stockId);
        e.setAdjustment(adjustment);
        e.setNotes(PayloadUtils.str(req, "notes"));
        e.setAdjustedBy(currentUser());
        e.setAdjustedAt(LocalDateTime.now());
        repo.save(e);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("stockId", e.getStockId());
        m.put("adjustment", e.getAdjustment());
        m.put("notes", e.getNotes());
        return ResponseEntity.ok(ApiResponse.success(m, "Adjustment saved"));
    }

    private String currentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
