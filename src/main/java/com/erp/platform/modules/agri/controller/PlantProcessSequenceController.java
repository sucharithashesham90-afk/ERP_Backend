package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePlantProcessSequenceRequest;
import com.erp.platform.modules.agri.dto.PlantProcessSequenceDto;
import com.erp.platform.modules.agri.service.PlantProcessSequenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/process-sequences")
@RequiredArgsConstructor
@Tag(name = "Plant Process Sequences", description = "Processing sequence definition per plant category")
public class PlantProcessSequenceController {

    private final PlantProcessSequenceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process sequences")
    public ResponseEntity<ApiResponse<PageResponse<PlantProcessSequenceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process sequence by ID")
    public ResponseEntity<ApiResponse<PlantProcessSequenceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process sequence")
    public ResponseEntity<ApiResponse<PlantProcessSequenceDto>> create(@Valid @RequestBody CreatePlantProcessSequenceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Process sequence created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process sequence")
    public ResponseEntity<ApiResponse<PlantProcessSequenceDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreatePlantProcessSequenceRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete process sequence")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Process sequence deleted"));
    }
}
