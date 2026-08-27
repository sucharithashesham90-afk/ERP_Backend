package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateSalesOrderRequest;
import com.erp.platform.modules.sales.dto.DeliveryNoteDto;
import com.erp.platform.modules.sales.dto.SalesOrderDto;
import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import com.erp.platform.modules.sales.service.DeliveryNoteService;
import com.erp.platform.modules.sales.service.SalesOrderService;
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
@RequestMapping("/api/v1/sales/orders")
@RequiredArgsConstructor
@Tag(name = "Sales - Orders", description = "Sales order management")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final DeliveryNoteService deliveryNoteService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales orders")
    public ResponseEntity<ApiResponse<PageResponse<SalesOrderDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SalesOrderStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sales order by ID")
    public ResponseEntity<ApiResponse<SalesOrderDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sales order")
    public ResponseEntity<ApiResponse<SalesOrderDto>> create(@Valid @RequestBody CreateSalesOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(salesOrderService.create(request), "Sales order created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales order")
    public ResponseEntity<ApiResponse<SalesOrderDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSalesOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales order status")
    public ResponseEntity<ApiResponse<SalesOrderDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        SalesOrderStatus status = SalesOrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(salesOrderService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/delivery-note")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a draft delivery note from this sales order")
    public ResponseEntity<ApiResponse<DeliveryNoteDto>> createDeliveryNote(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(deliveryNoteService.createFromSalesOrder(id), "Delivery note created"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete sales order (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        salesOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sales order deleted successfully"));
    }
}
