package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateTaxRequest;
import com.erp.platform.modules.master.dto.TaxDto;
import com.erp.platform.modules.master.service.TaxService;
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
@RequestMapping("/api/v1/master/taxes")
@RequiredArgsConstructor
@Tag(name = "Taxes", description = "Tax rate management")
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List tax rates")
    public ResponseEntity<ApiResponse<PageResponse<TaxDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(taxService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tax by ID")
    public ResponseEntity<ApiResponse<TaxDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taxService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new tax rate")
    public ResponseEntity<ApiResponse<TaxDto>> create(@Valid @RequestBody CreateTaxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taxService.create(request), "Tax created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update tax rate")
    public ResponseEntity<ApiResponse<TaxDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaxRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taxService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete tax rate (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taxService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tax deleted successfully"));
    }
}
