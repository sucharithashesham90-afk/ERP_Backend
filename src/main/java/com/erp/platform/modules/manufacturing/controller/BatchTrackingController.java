package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.manufacturing.entity.BatchEvent;
import com.erp.platform.modules.manufacturing.entity.BatchHistory;
import com.erp.platform.modules.manufacturing.entity.BatchHistory.BatchStatus;
import com.erp.platform.modules.manufacturing.service.BatchTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manufacturing/batches")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Batch Tracking", description = "Batch/lot lifecycle tracking")
public class BatchTrackingController {

    private final BatchTrackingService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List batches")
    public ResponseEntity<ApiResponse<PageResponse<BatchHistory>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) BatchStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.list(productId, status, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get batch by ID")
    public ResponseEntity<ApiResponse<BatchHistory>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create batch")
    public ResponseEntity<ApiResponse<BatchHistory>> create(@RequestBody BatchHistory req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update batch")
    public ResponseEntity<ApiResponse<BatchHistory>> update(@PathVariable UUID id, @RequestBody BatchHistory req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @GetMapping("/{id}/events")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get events for batch")
    public ResponseEntity<ApiResponse<List<BatchEvent>>> getEvents(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getEvents(id)));
    }

    @PostMapping("/{id}/events")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add event to batch")
    public ResponseEntity<ApiResponse<BatchEvent>> addEvent(@PathVariable UUID id, @RequestBody BatchEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.addEvent(id, event), "Event added"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update batch status")
    public ResponseEntity<ApiResponse<BatchHistory>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        BatchStatus newStatus = BatchStatus.valueOf(body.get("status"));
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(service.updateStatus(id, newStatus, reason)));
    }

    @PatchMapping("/{id}/adjust-quantity")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adjust batch remaining quantity")
    public ResponseEntity<ApiResponse<BatchHistory>> adjustQuantity(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        BigDecimal qty = new BigDecimal(body.get("quantity"));
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(service.adjustQuantity(id, qty, reason)));
    }
}
