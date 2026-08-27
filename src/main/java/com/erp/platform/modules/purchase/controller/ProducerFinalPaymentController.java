package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreateProducerFinalPaymentRequest;
import com.erp.platform.modules.purchase.dto.ProducerFinalPaymentDto;
import com.erp.platform.modules.purchase.service.ProducerFinalPaymentService;
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
@RequestMapping("/api/v1/purchase/producer-payments")
@RequiredArgsConstructor
@Tag(name = "Producer Final Payments", description = "Manage producer final payments")
public class ProducerFinalPaymentController {

    private final ProducerFinalPaymentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List producer final payments")
    public ResponseEntity<ApiResponse<PageResponse<ProducerFinalPaymentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("paymentNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProducerFinalPaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProducerFinalPaymentDto>> create(@Valid @RequestBody CreateProducerFinalPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "producerFinalPayment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProducerFinalPaymentDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateProducerFinalPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "producerFinalPayment deleted"));
    }
}
