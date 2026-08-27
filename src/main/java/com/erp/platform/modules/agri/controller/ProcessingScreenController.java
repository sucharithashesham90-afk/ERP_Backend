package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessingScreenRequest;
import com.erp.platform.modules.agri.dto.ProcessingScreenDto;
import com.erp.platform.modules.agri.service.ProcessingScreenService;
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
@RequestMapping("/api/v1/agri/processing-screens")
@RequiredArgsConstructor
@Tag(name = "Processing Screens", description = "Processing screen management")
public class ProcessingScreenController {

    private final ProcessingScreenService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List processing screens")
    public ResponseEntity<ApiResponse<PageResponse<ProcessingScreenDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get processing screen by ID")
    public ResponseEntity<ApiResponse<ProcessingScreenDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create processing screen")
    public ResponseEntity<ApiResponse<ProcessingScreenDto>> create(@Valid @RequestBody CreateProcessingScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Processing screen created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update processing screen")
    public ResponseEntity<ApiResponse<ProcessingScreenDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProcessingScreenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete processing screen")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Processing screen deleted"));
    }
}
