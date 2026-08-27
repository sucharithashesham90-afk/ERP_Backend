package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreateGoodsReceiptRequest;
import com.erp.platform.modules.purchase.dto.GoodsReceiptDto;
import com.erp.platform.modules.purchase.service.GoodsReceiptService;
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
@RequestMapping("/api/v1/purchase/goods-receipts")
@RequiredArgsConstructor
@Tag(name = "Purchase - Goods Receipts", description = "Goods receipt (GRN) management")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List goods receipts")
    public ResponseEntity<ApiResponse<PageResponse<GoodsReceiptDto>>> list(
            @RequestParam(required = false) UUID purchaseOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(goodsReceiptService.list(purchaseOrderId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get goods receipt by ID")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(goodsReceiptService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create goods receipt")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> create(@RequestBody CreateGoodsReceiptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goodsReceiptService.create(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete goods receipt")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        goodsReceiptService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
