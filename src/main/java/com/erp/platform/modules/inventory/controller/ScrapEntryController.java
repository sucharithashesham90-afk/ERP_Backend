package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateScrapDisposalRequest;
import com.erp.platform.modules.inventory.dto.CreateScrapEntryRequest;
import com.erp.platform.modules.inventory.dto.ScrapEntryDto;
import com.erp.platform.modules.inventory.entity.ScrapEntry.ScrapStatus;
import com.erp.platform.modules.inventory.service.ScrapEntryService;
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
@RequestMapping("/api/v1/inventory/scrap")
@RequiredArgsConstructor
@Tag(name = "Inventory - Scrap & Waste", description = "Scrap and waste management")
public class ScrapEntryController {

    private final ScrapEntryService scrapEntryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List scrap entries")
    public ResponseEntity<ApiResponse<PageResponse<ScrapEntryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ScrapStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scrapDate"));
        return ResponseEntity.ok(ApiResponse.success(scrapEntryService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get scrap entry by ID")
    public ResponseEntity<ApiResponse<ScrapEntryDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(scrapEntryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create scrap entry")
    public ResponseEntity<ApiResponse<ScrapEntryDto>> create(@RequestBody CreateScrapEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(scrapEntryService.create(request), "Scrap entry created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update scrap entry (DRAFT only)")
    public ResponseEntity<ApiResponse<ScrapEntryDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateScrapEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(scrapEntryService.update(id, request), "Scrap entry updated"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve scrap entry")
    public ResponseEntity<ApiResponse<ScrapEntryDto>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                scrapEntryService.approve(id, body.get("approvedBy")), "Scrap entry approved"));
    }

    @PostMapping("/{id}/dispose")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record scrap disposal")
    public ResponseEntity<ApiResponse<ScrapEntryDto>> dispose(
            @PathVariable UUID id,
            @RequestBody CreateScrapDisposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                scrapEntryService.recordDisposal(id, request), "Disposal recorded successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete scrap entry (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        scrapEntryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Scrap entry deleted successfully"));
    }
}
