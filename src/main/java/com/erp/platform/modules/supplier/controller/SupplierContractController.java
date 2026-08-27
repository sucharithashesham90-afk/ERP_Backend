package com.erp.platform.modules.supplier.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.supplier.dto.CreateSupplierContractRequest;
import com.erp.platform.modules.supplier.dto.SupplierContractDto;
import com.erp.platform.modules.supplier.entity.SupplierContract.ContractStatus;
import com.erp.platform.modules.supplier.service.SupplierContractService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/supplier/contracts")
@RequiredArgsConstructor
@Tag(name = "Supplier - Contracts")
public class SupplierContractController {

    private final SupplierContractService contractService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<SupplierContractDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) ContractStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(contractService.list(vendorId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierContractDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierContractDto>> create(@RequestBody CreateSupplierContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contractService.create(request), "Contract created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierContractDto>> update(
            @PathVariable UUID id, @RequestBody CreateSupplierContractRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contractService.update(id, request)));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierContractDto>> terminate(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                contractService.terminate(id, body.getOrDefault("reason", "")), "Contract terminated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        contractService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Contract deleted"));
    }
}
