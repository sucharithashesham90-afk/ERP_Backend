package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateCropDataRequest;
import com.erp.platform.modules.agri.dto.CropDataDto;
import com.erp.platform.modules.agri.service.CropDataService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/crop-data")
@RequiredArgsConstructor
@Tag(name = "Crop Data", description = "Manage agri crop data")
public class CropDataController {

    private final CropDataService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List crop data")
    public ResponseEntity<ApiResponse<PageResponse<CropDataDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID cropGroupId) {
        var pageable = PageRequest.of(page, size, Sort.by("cropName"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(cropGroupId, pageable)));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CropDataDto>>> listAll(
            @RequestParam(required = false) UUID cropGroupId) {
        return ResponseEntity.ok(ApiResponse.success(service.findAllList(cropGroupId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CropDataDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CropDataDto>> create(@Valid @RequestBody CreateCropDataRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "cropData created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CropDataDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateCropDataRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "cropData deleted"));
    }
}
