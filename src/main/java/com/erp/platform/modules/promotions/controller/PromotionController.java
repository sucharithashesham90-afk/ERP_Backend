package com.erp.platform.modules.promotions.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.promotions.dto.CreatePromotionRequest;
import com.erp.platform.modules.promotions.dto.PromotionDto;
import com.erp.platform.modules.promotions.service.PromotionService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Sales Promotions", description = "Manage sales promotions and schemes")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List promotions")
    public ResponseEntity<ApiResponse<PageResponse<PromotionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean active) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(promotionService.list(active, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get currently active promotions")
    public ResponseEntity<ApiResponse<List<PromotionDto>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getActivePromotions(LocalDate.now())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get promotion by ID")
    public ResponseEntity<ApiResponse<PromotionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create promotion")
    public ResponseEntity<ApiResponse<PromotionDto>> create(@Valid @RequestBody CreatePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(promotionService.create(request), "Promotion created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update promotion")
    public ResponseEntity<ApiResponse<PromotionDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.update(id, request), "Promotion updated successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Activate promotion")
    public ResponseEntity<ApiResponse<PromotionDto>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.activate(id), "Promotion activated"));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deactivate promotion")
    public ResponseEntity<ApiResponse<PromotionDto>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(promotionService.deactivate(id), "Promotion deactivated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete promotion (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Promotion deleted successfully"));
    }
}
