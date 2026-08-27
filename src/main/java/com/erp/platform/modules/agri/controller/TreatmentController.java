package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateTreatmentRequest;
import com.erp.platform.modules.agri.dto.TreatmentDto;
import com.erp.platform.modules.agri.service.TreatmentService;
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
@RequestMapping("/api/v1/agri/treatments")
@RequiredArgsConstructor
@Tag(name = "Treatments", description = "Treatment management")
public class TreatmentController {

    private final TreatmentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List treatments")
    public ResponseEntity<ApiResponse<PageResponse<TreatmentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get treatment by ID")
    public ResponseEntity<ApiResponse<TreatmentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create treatment")
    public ResponseEntity<ApiResponse<TreatmentDto>> create(@Valid @RequestBody CreateTreatmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Treatment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update treatment")
    public ResponseEntity<ApiResponse<TreatmentDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTreatmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete treatment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Treatment deleted"));
    }
}
