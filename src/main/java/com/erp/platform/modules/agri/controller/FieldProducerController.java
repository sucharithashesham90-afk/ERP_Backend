package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateFieldProducerRequest;
import com.erp.platform.modules.agri.dto.FieldProducerDto;
import com.erp.platform.modules.agri.service.FieldProducerService;
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
@RequestMapping("/api/v1/agri/field-producers")
@RequiredArgsConstructor
@Tag(name = "Field Producers", description = "Field producer (farmer) management")
public class FieldProducerController {

    private final FieldProducerService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List field producers")
    public ResponseEntity<ApiResponse<PageResponse<FieldProducerDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get field producer by ID")
    public ResponseEntity<ApiResponse<FieldProducerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create field producer")
    public ResponseEntity<ApiResponse<FieldProducerDto>> create(@Valid @RequestBody CreateFieldProducerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Field producer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update field producer")
    public ResponseEntity<ApiResponse<FieldProducerDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateFieldProducerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete field producer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Field producer deleted"));
    }
}
