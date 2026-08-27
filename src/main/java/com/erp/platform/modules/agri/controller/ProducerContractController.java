package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProducerContractRequest;
import com.erp.platform.modules.agri.dto.ProducerContractDto;
import com.erp.platform.modules.agri.service.ProducerContractService;
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
@RequestMapping("/api/v1/agri/producer-contracts")
@RequiredArgsConstructor
@Tag(name = "Producer Contracts", description = "Field producer contract management")
public class ProducerContractController {

    private final ProducerContractService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List producer contracts")
    public ResponseEntity<ApiResponse<PageResponse<ProducerContractDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("contractDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get producer contract by ID")
    public ResponseEntity<ApiResponse<ProducerContractDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create producer contract")
    public ResponseEntity<ApiResponse<ProducerContractDto>> create(@Valid @RequestBody CreateProducerContractRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Producer contract created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update producer contract")
    public ResponseEntity<ApiResponse<ProducerContractDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateProducerContractRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete producer contract")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producer contract deleted"));
    }
}
