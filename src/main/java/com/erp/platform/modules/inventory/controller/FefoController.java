package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.inventory.dto.ExpiryOverviewDto;
import com.erp.platform.modules.inventory.dto.FefoAllocationDto;
import com.erp.platform.modules.inventory.service.FefoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/fefo")
@RequiredArgsConstructor
@Tag(name = "Inventory - FEFO", description = "First-expiry-first-out allocation and shelf-life reporting")
public class FefoController {

    private final FefoService fefoService;

    @GetMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Preview which lots a dispatch would draw from, soonest expiry first")
    public ResponseEntity<ApiResponse<FefoAllocationDto>> preview(
            @RequestParam UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(ApiResponse.success(fefoService.plan(productId, warehouseId, quantity)));
    }

    @GetMapping("/expiry-overview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Shelf-life overview of lots on hand, bucketed by closeness to expiry")
    public ResponseEntity<ApiResponse<ExpiryOverviewDto>> expiryOverview(
            @RequestParam(required = false) Integer horizonDays,
            @RequestParam(required = false) UUID warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(fefoService.expiryOverview(horizonDays, warehouseId)));
    }

    @PostMapping("/mark-expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN', 'INVENTORY_MANAGER')")
    @Operation(summary = "Flag lots past their expiry date so FEFO stops offering them")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markExpired() {
        int marked = fefoService.markExpiredLots();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("markedExpired", marked),
                marked == 0 ? "No lots past expiry" : marked + " lot(s) marked expired"));
    }
}
