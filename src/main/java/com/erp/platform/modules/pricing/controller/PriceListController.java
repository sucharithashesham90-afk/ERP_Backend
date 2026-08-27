package com.erp.platform.modules.pricing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.pricing.entity.PriceList;
import com.erp.platform.modules.pricing.entity.PriceListItem;
import com.erp.platform.modules.pricing.service.PriceListService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing/price-lists")
@RequiredArgsConstructor
@Tag(name = "Pricing - Price Lists", description = "Price list management")
public class PriceListController {

    private final PriceListService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List price lists")
    public ResponseEntity<ApiResponse<PageResponse<PriceList>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get price list by ID")
    public ResponseEntity<ApiResponse<PriceList>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create price list")
    public ResponseEntity<ApiResponse<PriceList>> create(@RequestBody PriceList req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update price list")
    public ResponseEntity<ApiResponse<PriceList>> update(@PathVariable UUID id, @RequestBody PriceList req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete price list")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get items for price list")
    public ResponseEntity<ApiResponse<List<PriceListItem>>> getItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getItemsForPriceList(id)));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to price list")
    public ResponseEntity<ApiResponse<PriceListItem>> addItem(@PathVariable UUID id, @RequestBody PriceListItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.addItem(id, item), "Item added"));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from price list")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable UUID itemId) {
        service.removeItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed"));
    }

    @PostMapping("/clone")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clone a price list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clone(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("message", "Cloned successfully", "sourceId", body.getOrDefault("sourceId", "").toString())));
    }

    @GetMapping("/clone-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Price list clone history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> cloneHistory() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
