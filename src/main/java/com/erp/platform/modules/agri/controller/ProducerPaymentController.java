package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProducerPaymentRequest;
import com.erp.platform.modules.agri.dto.DeductionItemRequest;
import com.erp.platform.modules.agri.dto.ProducerPaymentDto;
import com.erp.platform.modules.agri.service.ProducerPaymentService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/producer-payments")
@RequiredArgsConstructor
@Tag(name = "Producer Payments", description = "Payment management for field producers")
public class ProducerPaymentController {

    private final ProducerPaymentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List producer payments")
    public ResponseEntity<ApiResponse<PageResponse<ProducerPaymentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("paymentDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get producer payment by ID")
    public ResponseEntity<ApiResponse<ProducerPaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create producer payment")
    public ResponseEntity<ApiResponse<ProducerPaymentDto>> create(@Valid @RequestBody CreateProducerPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Producer payment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update producer payment")
    public ResponseEntity<ApiResponse<ProducerPaymentDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateProducerPaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete producer payment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producer payment deleted"));
    }

    @PostMapping("/{id}/calculate-deductions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calculate and save itemized deductions for a payment",
               description = "Replaces existing deduction items. Accepts PERCENTAGE, PER_UNIT, WEIGHT, or FIXED deduction types. Updates deductions total and netAmount.")
    public ResponseEntity<ApiResponse<ProducerPaymentDto>> calculateDeductions(
            @PathVariable UUID id,
            @RequestBody List<DeductionItemRequest> deductions) {
        return ResponseEntity.ok(ApiResponse.success(
                service.calculateDeductions(id, deductions),
                "Deductions calculated and saved"));
    }
}
