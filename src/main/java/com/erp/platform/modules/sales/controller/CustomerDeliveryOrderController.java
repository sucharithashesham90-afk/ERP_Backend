package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateCustomerDeliveryOrderRequest;
import com.erp.platform.modules.sales.dto.CustomerDeliveryOrderDto;
import com.erp.platform.modules.sales.service.CustomerDeliveryOrderService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/delivery-orders")
@RequiredArgsConstructor
@Tag(name = "Customer Delivery Orders", description = "Customer delivery order management")
public class CustomerDeliveryOrderController {

    private final CustomerDeliveryOrderService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customer delivery orders")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDeliveryOrderDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("doDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer delivery order by ID")
    public ResponseEntity<ApiResponse<CustomerDeliveryOrderDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create customer delivery order")
    public ResponseEntity<ApiResponse<CustomerDeliveryOrderDto>> create(@Valid @RequestBody CreateCustomerDeliveryOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Delivery order created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update customer delivery order")
    public ResponseEntity<ApiResponse<CustomerDeliveryOrderDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCustomerDeliveryOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete customer delivery order")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Delivery order deleted"));
    }
}
