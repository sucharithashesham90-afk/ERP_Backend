package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.entity.ServiceCategory;
import com.erp.platform.modules.purchase.entity.ServiceDefinition;
import com.erp.platform.modules.purchase.entity.ServiceOrder;
import com.erp.platform.modules.purchase.service.ServicePurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequiredArgsConstructor
@Tag(name = "Purchase - Service Purchase", description = "Service purchase management")
public class ServicePurchaseController {

    private final ServicePurchaseService service;

    // ---- Service Categories ----

    @GetMapping("/api/v1/purchase/service-categories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List service categories")
    public ResponseEntity<ApiResponse<PageResponse<ServiceCategory>>> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listCategories(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/purchase/service-categories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create service category")
    public ResponseEntity<ApiResponse<ServiceCategory>> createCategory(@RequestBody ServiceCategory req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createCategory(req), "Created"));
    }

    @PutMapping("/api/v1/purchase/service-categories/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update service category")
    public ResponseEntity<ApiResponse<ServiceCategory>> updateCategory(@PathVariable UUID id, @RequestBody ServiceCategory req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateCategory(id, req)));
    }

    @DeleteMapping("/api/v1/purchase/service-categories/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete service category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        service.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Service Definitions ----

    @GetMapping("/api/v1/purchase/services")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List services")
    public ResponseEntity<ApiResponse<PageResponse<ServiceDefinition>>> listServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(service.listServices(categoryId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/purchase/services")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create service definition")
    public ResponseEntity<ApiResponse<ServiceDefinition>> createService(@RequestBody ServiceDefinition req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createService(req), "Created"));
    }

    @PutMapping("/api/v1/purchase/services/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update service definition")
    public ResponseEntity<ApiResponse<ServiceDefinition>> updateService(@PathVariable UUID id, @RequestBody ServiceDefinition req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateService(id, req)));
    }

    @DeleteMapping("/api/v1/purchase/services/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete service definition")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable UUID id) {
        service.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Service Orders ----

    @GetMapping("/api/v1/purchase/service-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List service orders")
    public ResponseEntity<ApiResponse<PageResponse<ServiceOrder>>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listOrders(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/purchase/service-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get service order by ID")
    public ResponseEntity<ApiResponse<ServiceOrder>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getOrder(id)));
    }

    @PostMapping("/api/v1/purchase/service-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create service order")
    public ResponseEntity<ApiResponse<ServiceOrder>> createOrder(@RequestBody ServiceOrder req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createOrder(req), "Created"));
    }

    @PutMapping("/api/v1/purchase/service-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update service order (DRAFT or APPROVED only)")
    public ResponseEntity<ApiResponse<ServiceOrder>> updateOrder(@PathVariable UUID id, @RequestBody ServiceOrder req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOrder(id, req)));
    }

    @PatchMapping("/api/v1/purchase/service-orders/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update service order status")
    public ResponseEntity<ApiResponse<ServiceOrder>> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOrderStatus(id, body.get("status"))));
    }

    @DeleteMapping("/api/v1/purchase/service-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete service order")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID id) {
        service.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
