package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePurchaseReturnRequest;
import com.erp.platform.modules.purchase.dto.PurchaseReturnDto;
import com.erp.platform.modules.purchase.service.PurchaseReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/returns")
@RequiredArgsConstructor
@Tag(name = "Purchase - Returns", description = "Purchase return management")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase returns")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseReturnDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(purchaseReturnService.list(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase return")
    public ResponseEntity<ApiResponse<PurchaseReturnDto>> create(@RequestBody CreatePurchaseReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(purchaseReturnService.create(request)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve purchase return and deduct stock")
    public ResponseEntity<ApiResponse<PurchaseReturnDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseReturnService.approve(id), "Purchase return approved — stock deducted"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete purchase return")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        purchaseReturnService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
