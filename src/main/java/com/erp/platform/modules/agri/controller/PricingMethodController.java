package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePricingMethodRequest;
import com.erp.platform.modules.agri.dto.PricingMethodDto;
import com.erp.platform.modules.agri.service.PricingMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/pricing-methods")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PricingMethodController {

    private final PricingMethodService pricingMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PricingMethodDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(pricingMethodService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingMethodDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(pricingMethodService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PricingMethodDto>> create(@Valid @RequestBody CreatePricingMethodRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pricingMethodService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PricingMethodDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePricingMethodRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pricingMethodService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        pricingMethodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
