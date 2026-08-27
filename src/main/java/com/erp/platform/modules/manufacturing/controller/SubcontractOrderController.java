package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.manufacturing.entity.SubcontractOrder;
import com.erp.platform.modules.manufacturing.entity.SubcontractOrder.SCOStatus;
import com.erp.platform.modules.manufacturing.service.SubcontractOrderService;
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
@RequestMapping("/api/v1/manufacturing/subcontract-orders")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Subcontract Orders", description = "Outsourced manufacturing orders")
public class SubcontractOrderController {

    private final SubcontractOrderService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List subcontract orders")
    public ResponseEntity<ApiResponse<PageResponse<SubcontractOrder>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        SCOStatus st = status != null ? SCOStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.success(service.list(st, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubcontractOrder>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create subcontract order")
    public ResponseEntity<ApiResponse<SubcontractOrder>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(body), "Subcontract order created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubcontractOrder>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, body)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update subcontract order status")
    public ResponseEntity<ApiResponse<SubcontractOrder>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        SCOStatus newStatus = SCOStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(service.updateStatus(id, newStatus)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
