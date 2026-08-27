package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePackingAdviceRequest;
import com.erp.platform.modules.agri.dto.PackingAdviceDto;
import com.erp.platform.modules.agri.service.PackingAdviceService;
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
@RequestMapping("/api/v1/agri/packing-advices")
@RequiredArgsConstructor
@Tag(name = "Packing Advices", description = "Manage packing advices")
public class PackingAdviceController {

    private final PackingAdviceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List packing advices")
    public ResponseEntity<ApiResponse<PageResponse<PackingAdviceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("adviceNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackingAdviceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackingAdviceDto>> create(@Valid @RequestBody CreatePackingAdviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "packingAdvice created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackingAdviceDto>> update(@PathVariable UUID id, @Valid @RequestBody CreatePackingAdviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "packingAdvice deleted"));
    }
}
