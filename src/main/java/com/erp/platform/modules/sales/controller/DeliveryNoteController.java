package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateDeliveryNoteRequest;
import com.erp.platform.modules.sales.dto.DeliveryNoteDto;
import com.erp.platform.modules.sales.service.DeliveryNoteService;
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
@RequestMapping("/api/v1/sales/delivery-notes")
@RequiredArgsConstructor
@Tag(name = "Sales - Delivery Notes", description = "Delivery note management")
public class DeliveryNoteController {

    private final DeliveryNoteService deliveryNoteService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List delivery notes")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryNoteDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(deliveryNoteService.list(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create delivery note")
    public ResponseEntity<ApiResponse<DeliveryNoteDto>> create(@RequestBody CreateDeliveryNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(deliveryNoteService.create(request)));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark delivery note as DELIVERED and deduct stock")
    public ResponseEntity<ApiResponse<DeliveryNoteDto>> deliver(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(deliveryNoteService.deliver(id), "Delivery completed — stock deducted"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete delivery note")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deliveryNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
