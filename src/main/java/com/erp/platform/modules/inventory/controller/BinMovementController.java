package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.inventory.service.BinMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Bin-level movements for the warehouse scanner.
 *
 * <p>Deliberately small and blunt: each call is one scan-and-go action, because the caller is a
 * phone held in one hand by someone standing in front of a rack.
 */
@RestController
@RequestMapping("/api/v1/inventory/bin")
@RequiredArgsConstructor
@Tag(name = "Inventory - Bin Movements", description = "Scanner put-away, transfer and pick")
public class BinMovementController {

    private final BinMovementService binMovementService;

    @GetMapping("/lot/{lotNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "What a scanned lot is, and which bins hold it")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lookupLot(@PathVariable String lotNumber) {
        return ResponseEntity.ok(ApiResponse.success(binMovementService.lookupLot(lotNumber)));
    }

    @GetMapping("/location/{locationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "What is currently in a bin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lookupBin(@PathVariable UUID locationId) {
        return ResponseEntity.ok(ApiResponse.success(binMovementService.lookupBin(locationId)));
    }

    @PostMapping("/put-away")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Put a lot into a bin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> putAway(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(
                binMovementService.putAway(str(body, "lotNumber"), uuid(body, "locationId"),
                        dec(body, "quantity"), bool(body, "overrideVarietyCheck")),
                "Put away"));
    }

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Move a lot from one bin to another")
    public ResponseEntity<ApiResponse<Map<String, Object>>> transfer(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(
                binMovementService.transfer(str(body, "lotNumber"), uuid(body, "fromLocationId"),
                        uuid(body, "toLocationId"), dec(body, "quantity"),
                        bool(body, "overrideVarietyCheck")),
                "Transferred"));
    }

    @PostMapping("/pick")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Take stock out of a bin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pick(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(
                binMovementService.pick(str(body, "lotNumber"), uuid(body, "locationId"),
                        dec(body, "quantity")),
                "Picked"));
    }

    private static String str(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v == null ? null : v.toString();
    }

    private static UUID uuid(Map<String, Object> b, String k) {
        String s = str(b, k);
        return s == null || s.isBlank() ? null : UUID.fromString(s);
    }

    private static BigDecimal dec(Map<String, Object> b, String k) {
        String s = str(b, k);
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static boolean bool(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v instanceof Boolean bo ? bo : "true".equalsIgnoreCase(String.valueOf(v));
    }
}
