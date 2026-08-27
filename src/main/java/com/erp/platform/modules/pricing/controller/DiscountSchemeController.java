package com.erp.platform.modules.pricing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.pricing.entity.DiscountScheme;
import com.erp.platform.modules.pricing.entity.DiscountSlab;
import com.erp.platform.modules.pricing.service.DiscountSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing/discount-schemes")
@RequiredArgsConstructor
@Tag(name = "Pricing - Discount Schemes", description = "Discount scheme management")
public class DiscountSchemeController {

    private final DiscountSchemeService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List discount schemes")
    public ResponseEntity<ApiResponse<PageResponse<DiscountScheme>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get discount scheme by ID")
    public ResponseEntity<ApiResponse<DiscountScheme>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create discount scheme")
    public ResponseEntity<ApiResponse<DiscountScheme>> create(@RequestBody DiscountScheme req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update discount scheme")
    public ResponseEntity<ApiResponse<DiscountScheme>> update(@PathVariable UUID id, @RequestBody DiscountScheme req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete discount scheme")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/{id}/slabs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get slabs for discount scheme")
    public ResponseEntity<ApiResponse<List<DiscountSlab>>> getSlabs(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getSlabs(id)));
    }

    @PostMapping("/{id}/slabs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add slab to discount scheme")
    public ResponseEntity<ApiResponse<DiscountSlab>> addSlab(@PathVariable UUID id, @RequestBody DiscountSlab slab) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.addSlab(id, slab), "Slab added"));
    }

    @DeleteMapping("/slabs/{slabId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove slab from discount scheme")
    public ResponseEntity<ApiResponse<Void>> removeSlab(@PathVariable UUID slabId) {
        service.removeSlab(slabId);
        return ResponseEntity.ok(ApiResponse.success(null, "Slab removed"));
    }
}
