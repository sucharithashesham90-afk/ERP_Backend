package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.entity.SalesTarget;
import com.erp.platform.modules.sales.entity.SalesTarget.TargetStatus;
import com.erp.platform.modules.sales.service.SalesTargetService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/targets")
@RequiredArgsConstructor
@Tag(name = "Sales - Targets", description = "Sales target management")
public class SalesTargetController {

    private final SalesTargetService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales targets")
    public ResponseEntity<ApiResponse<PageResponse<SalesTarget>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TargetStatus status) {
        return ResponseEntity.ok(ApiResponse.success(service.list(status, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get target by ID")
    public ResponseEntity<ApiResponse<SalesTarget>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sales target")
    public ResponseEntity<ApiResponse<SalesTarget>> create(@RequestBody SalesTarget req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales target")
    public ResponseEntity<ApiResponse<SalesTarget>> update(@PathVariable UUID id, @RequestBody SalesTarget req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete sales target")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PatchMapping("/{id}/actuals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update actual amounts for target")
    public ResponseEntity<ApiResponse<SalesTarget>> updateActuals(
            @PathVariable UUID id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal actualAmount = body.get("actualAmount");
        BigDecimal actualQuantity = body.get("actualQuantity");
        return ResponseEntity.ok(ApiResponse.success(service.updateActuals(id, actualAmount, actualQuantity)));
    }
}
