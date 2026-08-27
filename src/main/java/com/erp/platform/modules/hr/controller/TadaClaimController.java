package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateTadaClaimRequest;
import com.erp.platform.modules.hr.dto.TadaClaimDto;
import com.erp.platform.modules.hr.service.TadaClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/tada-claims")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TadaClaimController {

    private final TadaClaimService tadaClaimService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TadaClaimDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(tadaClaimService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TadaClaimDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tadaClaimService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TadaClaimDto>> create(@Valid @RequestBody CreateTadaClaimRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tadaClaimService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TadaClaimDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTadaClaimRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tadaClaimService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        tadaClaimService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
