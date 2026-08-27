package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreateLotPurchaseReturnRequest;
import com.erp.platform.modules.purchase.dto.LotPurchaseReturnDto;
import com.erp.platform.modules.purchase.service.LotPurchaseReturnService;
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
@RequestMapping("/api/v1/purchase/lot-purchase-returns")
@RequiredArgsConstructor
@Tag(name = "Lot Purchase Returns", description = "Manage lot purchase returns")
public class LotPurchaseReturnController {

    private final LotPurchaseReturnService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List lot purchase returns")
    public ResponseEntity<ApiResponse<PageResponse<LotPurchaseReturnDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("returnNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotPurchaseReturnDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotPurchaseReturnDto>> create(@Valid @RequestBody CreateLotPurchaseReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "lotPurchaseReturn created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotPurchaseReturnDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateLotPurchaseReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "lotPurchaseReturn deleted"));
    }
}
