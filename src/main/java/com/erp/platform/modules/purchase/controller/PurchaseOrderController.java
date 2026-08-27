package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePurchaseOrderRequest;
import com.erp.platform.modules.purchase.dto.PurchaseOrderDto;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import com.erp.platform.modules.purchase.service.PurchaseOrderService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/orders")
@RequiredArgsConstructor
@Tag(name = "Purchase - Orders", description = "Purchase order management")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase orders")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) POStatus status,
            @RequestParam(required = false) java.util.UUID supplierId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(poService.list(supplierId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(poService.getById(id)));
    }

    @GetMapping("/by-number/{poNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase order by PO number")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getByNumber(@PathVariable String poNumber) {
        return ResponseEntity.ok(ApiResponse.success(poService.getByNumber(poNumber)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(poService.create(request), "Purchase order created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(poService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update purchase order status")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        POStatus status = POStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(poService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record goods receipt for a PO")
    public ResponseEntity<ApiResponse<GoodsReceipt>> receiveGoods(
            @PathVariable UUID id,
            @RequestBody GoodsReceipt grn) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(poService.receiveGoods(id, grn), "Goods received successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete purchase order (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        poService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase order deleted successfully"));
    }
}
