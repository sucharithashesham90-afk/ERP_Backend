package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.LotMovementDto;
import com.erp.platform.modules.inventory.dto.LotStockDto;
import com.erp.platform.modules.inventory.dto.LotTraceDto;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.service.LotTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/inventory/lots", "/api/v1/inventory/lot-stock"})
@RequiredArgsConstructor
@Tag(name = "Inventory - Lot Tracking", description = "Lot tracking and traceability")
public class LotTrackingController {

    private final LotTrackingService lotTrackingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List lots with optional product/warehouse/location/name/lotNumber/status filter")
    public ResponseEntity<ApiResponse<PageResponse<LotStockDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            /** Location by name — what the transfer screens hold, where warehouseId is an id they do not have. */
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) String status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        LotStock.LotStockStatus statusEnum = (status != null && !status.isBlank())
                ? LotStock.LotStockStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.success(
                lotTrackingService.listLots(productId, warehouseId, location, productName, lotNumber, statusEnum, pageable)));
    }

    @GetMapping("/trace")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Full trace for a lot number — stocks + chronological movement history + summary")
    public ResponseEntity<ApiResponse<LotTraceDto>> trace(@RequestParam String lotNumber) {
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.traceLot(lotNumber)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get lot by ID")
    public ResponseEntity<ApiResponse<LotStockDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.getLotById(id)));
    }

    @GetMapping("/by-lot/{lotNumber}/movements")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get movements for a lot number")
    public ResponseEntity<ApiResponse<PageResponse<LotMovementDto>>> getLotMovements(
            @PathVariable String lotNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "movementDate"));
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.getLotMovements(lotNumber, pageable)));
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adjust lot quantity")
    public ResponseEntity<ApiResponse<LotStockDto>> adjust(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        BigDecimal qty = new BigDecimal(body.get("quantity"));
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.adjustLot(id, qty, reason), "Lot adjusted successfully"));
    }

    @PostMapping("/{id}/quarantine")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Quarantine a lot")
    public ResponseEntity<ApiResponse<LotStockDto>> quarantine(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.quarantineLot(id, reason), "Lot quarantined successfully"));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Release a quarantined lot")
    public ResponseEntity<ApiResponse<LotStockDto>> release(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(lotTrackingService.releaseLot(id), "Lot released successfully"));
    }
}
