package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.entity.StockMovement;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.inventory.repository.StockTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Recording a stock movement by hand.
 *
 * <p>Stock Movements was a report: it listed what had moved and gave no way to record a movement.
 * Everything reached it as a side effect of something else — a receipt, an issue, a transfer — so a
 * correction, a sample drawn, or seed moved between godowns for a reason the system has no document
 * for could not be entered at all.
 *
 * <p>Modelled on the seedflow inventory movement: a material at a location and godown, in a state
 * and of a type, moved with a narration saying why. A movement against a known lot adjusts that
 * lot's quantity, so the movement and the stock it describes cannot disagree.
 */
@RestController
@RequestMapping("/api/v1/inventory/stock-movements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory - Stock Movement Entry")
public class StockMovementEntryController {

    /** StockMovement rows live behind this repository, despite its name. */
    private final StockTransactionRepository movementRepository;
    private final StockLotRepository stockLotRepository;
    private final GodownRepository godownRepository;
    private final TenantContext tenantContext;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Record a stock movement")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();

        StockMovement.MovementType type = parseType(str(req, "type"));
        BigDecimal qty = dec(req, "quantity");
        if (qty == null || qty.signum() <= 0) throw AppException.badRequest("Quantity must be greater than zero");

        String lotNumber = str(req, "lotNumber");
        UUID godownId = uuid(req, "godownId");
        if (godownId == null) throw AppException.badRequest("Godown is required");

        String godownName = godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, godownId)
                .map(g -> g.getName()).orElse(str(req, "godownName"));

        // A movement against a known lot moves that lot, so the two cannot drift apart. Without a
        // lot there is nothing to adjust and the movement is recorded on its own.
        StockLot lot = null;
        if (lotNumber != null && !lotNumber.isBlank()) {
            List<StockLot> lots = stockLotRepository
                    .findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, lotNumber.trim());
            lot = lots.isEmpty() ? null : lots.get(0);
            if (lot == null) throw AppException.badRequest("No such lot: " + lotNumber);
        }

        BigDecimal before = lot != null && lot.getQuantity() != null ? lot.getQuantity() : BigDecimal.ZERO;
        BigDecimal after = before;
        if (lot != null) {
            after = switch (type) {
                case RECEIPT, TRANSFER_IN -> before.add(qty);
                case ISSUE, TRANSFER_OUT -> {
                    if (before.compareTo(qty) < 0) {
                        throw AppException.insufficientStock("Lot " + lotNumber + " holds only "
                                + before.stripTrailingZeros().toPlainString());
                    }
                    yield before.subtract(qty);
                }
                case ADJUSTMENT -> qty;      // an adjustment states the count rather than the change
            };
            lot.setQuantity(after);
            stockLotRepository.save(lot);
        }

        StockMovement m = new StockMovement();
        m.setTenantId(tenantId);
        m.setType(type);
        m.setWarehouseId(godownId);
        m.setWarehouseName(godownName);
        m.setProductId(lot != null ? lot.getProductId() : uuid(req, "productId"));
        m.setProductName(lot != null ? lot.getProductName() : str(req, "productName"));
        m.setLotNumber(lotNumber);
        m.setQuantity(qty);
        m.setBalanceBefore(before);
        m.setBalanceAfter(after);
        m.setMaterialType(str(req, "materialType"));
        m.setMaterialState(str(req, "materialState"));
        m.setPurposeType(str(req, "purposeType"));
        m.setReferenceType("MANUAL");
        m.setReferenceNumber(str(req, "referenceNumber"));
        m.setNotes(str(req, "narration"));
        LocalDate d = date(req, "movementDate");
        m.setMovementDate(d != null ? d : LocalDate.now());
        movementRepository.save(m);

        log.info("Stock movement recorded: {} {} of lot {} at {}", type, qty, lotNumber, godownName);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("type", type.name());
        out.put("lotNumber", lotNumber);
        out.put("quantity", qty);
        out.put("balanceBefore", before);
        out.put("balanceAfter", after);
        out.put("godownName", godownName);
        out.put("movementDate", m.getMovementDate().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(out, "Stock movement recorded"));
    }

    private static StockMovement.MovementType parseType(String v) {
        if (v == null || v.isBlank()) throw AppException.badRequest("Movement type is required");
        try { return StockMovement.MovementType.valueOf(v.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { throw AppException.badRequest("Unknown movement type: " + v); }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
    }

    private static UUID uuid(Map<String, Object> m, String k) {
        String s = str(m, k);
        try { return s == null ? null : UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }

    private static BigDecimal dec(Map<String, Object> m, String k) {
        String s = str(m, k);
        try { return s == null ? null : new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    private static LocalDate date(Map<String, Object> m, String k) {
        String s = str(m, k);
        try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
