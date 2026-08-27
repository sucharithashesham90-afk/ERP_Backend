package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.purchase.service.ThreeWayMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/three-way-match")
@RequiredArgsConstructor
@Tag(name = "Purchase - Three-Way Match", description = "PO vs GRN vs Invoice reconciliation")
public class ThreeWayMatchController {

    private final ThreeWayMatchService threeWayMatchService;

    @GetMapping("/{purchaseOrderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Three-way match for a PO: compares PO qty, GRN accepted qty, and invoice amount")
    public ResponseEntity<ApiResponse<Map<String, Object>>> match(@PathVariable UUID purchaseOrderId) {
        return ResponseEntity.ok(ApiResponse.success(threeWayMatchService.match(purchaseOrderId)));
    }
}
