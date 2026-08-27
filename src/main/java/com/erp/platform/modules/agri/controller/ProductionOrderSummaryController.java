package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProductionOrderSummaryRequest;
import com.erp.platform.modules.agri.dto.ProductionOrderSummaryDto;
import com.erp.platform.modules.agri.service.ProductionOrderSummaryService;
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
@RequestMapping("/api/v1/agri/production-order-summaries")
@RequiredArgsConstructor
@Tag(name = "Production Order Summaries", description = "Manage agri production order summaries")
public class ProductionOrderSummaryController {

    private final ProductionOrderSummaryService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List production order summaries")
    public ResponseEntity<ApiResponse<PageResponse<ProductionOrderSummaryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("orderNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionOrderSummaryDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionOrderSummaryDto>> create(@Valid @RequestBody CreateProductionOrderSummaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "productionOrderSummary created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionOrderSummaryDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateProductionOrderSummaryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "productionOrderSummary deleted"));
    }
}
