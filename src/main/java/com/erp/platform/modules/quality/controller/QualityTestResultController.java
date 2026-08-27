package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.CreateQualityTestResultRequest;
import com.erp.platform.modules.quality.dto.QualityTestResultDto;
import com.erp.platform.modules.quality.service.QualityTestResultService;
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
@RequestMapping("/api/v1/quality/test-results")
@RequiredArgsConstructor
@Tag(name = "Quality Test Results", description = "Manage quality test results")
public class QualityTestResultController {

    private final QualityTestResultService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List quality test results")
    public ResponseEntity<ApiResponse<PageResponse<QualityTestResultDto>>> list(
            @RequestParam(required = false, defaultValue = "") String lot,
            @RequestParam(required = false, defaultValue = "") String product,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String result,   // accepted for UI compatibility; no stored result field
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        java.time.LocalDate fromD = (from != null && !from.isBlank()) ? java.time.LocalDate.parse(from) : null;
        java.time.LocalDate toD = (to != null && !to.isBlank()) ? java.time.LocalDate.parse(to) : null;
        var pageable = PageRequest.of(page, size, Sort.by("testResultNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.search(lot, product, fromD, toD, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QualityTestResultDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QualityTestResultDto>> create(@Valid @RequestBody CreateQualityTestResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "qualityTestResult created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QualityTestResultDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateQualityTestResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "qualityTestResult deleted"));
    }
}
