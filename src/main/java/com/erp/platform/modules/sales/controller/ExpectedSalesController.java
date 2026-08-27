package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateExpectedSalesRequest;
import com.erp.platform.modules.sales.dto.ExpectedSalesDto;
import com.erp.platform.modules.sales.service.ExpectedSalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/expected-sales")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ExpectedSalesController {

    private final ExpectedSalesService expectedSalesService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpectedSalesDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(expectedSalesService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpectedSalesDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(expectedSalesService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpectedSalesDto>> create(@Valid @RequestBody CreateExpectedSalesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(expectedSalesService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpectedSalesDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateExpectedSalesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(expectedSalesService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        expectedSalesService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
