package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateLogisticsPaymentRequest;
import com.erp.platform.modules.agri.dto.LogisticsPaymentDto;
import com.erp.platform.modules.agri.service.LogisticsPaymentService;
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
@RequestMapping("/api/v1/agri/logistics-payments")
@RequiredArgsConstructor
@Tag(name = "Logistics Payments", description = "Handling and logistics payment management")
public class LogisticsPaymentController {

    private final LogisticsPaymentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List logistics payments")
    public ResponseEntity<ApiResponse<PageResponse<LogisticsPaymentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("paymentDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get logistics payment by ID")
    public ResponseEntity<ApiResponse<LogisticsPaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create logistics payment")
    public ResponseEntity<ApiResponse<LogisticsPaymentDto>> create(@Valid @RequestBody CreateLogisticsPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Logistics payment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update logistics payment")
    public ResponseEntity<ApiResponse<LogisticsPaymentDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateLogisticsPaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete logistics payment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Logistics payment deleted"));
    }
}
