package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateStockTransferRequest;
import com.erp.platform.modules.inventory.dto.StockTransferDto;
import com.erp.platform.modules.inventory.service.StockTransferService;
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
@RequestMapping("/api/v1/inventory/transfers")
@RequiredArgsConstructor
@Tag(name = "Inventory - Stock Transfers", description = "Inter-warehouse stock transfer management")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List stock transfers")
    public ResponseEntity<ApiResponse<PageResponse<StockTransferDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.list(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create stock transfer")
    public ResponseEntity<ApiResponse<StockTransferDto>> create(@RequestBody CreateStockTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(stockTransferService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update stock transfer (DRAFT only)")
    public ResponseEntity<ApiResponse<StockTransferDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateStockTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.update(id, request)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm stock transfer (moves stock from source to destination)")
    public ResponseEntity<ApiResponse<StockTransferDto>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockTransferService.confirm(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete stock transfer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        stockTransferService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
