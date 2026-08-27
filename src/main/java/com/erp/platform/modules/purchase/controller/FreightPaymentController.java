package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreateFreightPaymentRequest;
import com.erp.platform.modules.purchase.dto.FreightPaymentDto;
import com.erp.platform.modules.purchase.service.FreightPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/freight-payments")
@RequiredArgsConstructor
@Tag(name = "Purchase - Freight Payments", description = "Freight payment management")
public class FreightPaymentController {

    private final FreightPaymentService freightPaymentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List freight payments")
    public ResponseEntity<ApiResponse<PageResponse<FreightPaymentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(freightPaymentService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get freight payment by ID")
    public ResponseEntity<ApiResponse<FreightPaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(freightPaymentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create freight payment")
    public ResponseEntity<ApiResponse<FreightPaymentDto>> create(@RequestBody CreateFreightPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(freightPaymentService.create(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete freight payment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        freightPaymentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
