package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateDemandForecastRequest;
import com.erp.platform.modules.sales.dto.DemandForecastDto;
import com.erp.platform.modules.sales.entity.DemandForecast.ForecastStatus;
import com.erp.platform.modules.sales.service.DemandForecastService;
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
@RequestMapping("/api/v1/sales/demand-forecasts")
@RequiredArgsConstructor
@Tag(name = "Demand Forecasts", description = "Sales demand forecasting management")
public class DemandForecastController {

    private final DemandForecastService demandForecastService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List demand forecasts")
    public ResponseEntity<ApiResponse<PageResponse<DemandForecastDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID productId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(required = false) ForecastStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(demandForecastService.list(productId, year, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get demand forecast by ID")
    public ResponseEntity<ApiResponse<DemandForecastDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(demandForecastService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new demand forecast")
    public ResponseEntity<ApiResponse<DemandForecastDto>> create(@Valid @RequestBody CreateDemandForecastRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(demandForecastService.create(request), "Demand forecast created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update demand forecast")
    public ResponseEntity<ApiResponse<DemandForecastDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDemandForecastRequest request) {
        return ResponseEntity.ok(ApiResponse.success(demandForecastService.update(id, request), "Demand forecast updated successfully"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Publish demand forecast")
    public ResponseEntity<ApiResponse<DemandForecastDto>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(demandForecastService.publish(id), "Demand forecast published"));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archive demand forecast")
    public ResponseEntity<ApiResponse<DemandForecastDto>> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(demandForecastService.archive(id), "Demand forecast archived"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete demand forecast (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        demandForecastService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Demand forecast deleted successfully"));
    }
}
