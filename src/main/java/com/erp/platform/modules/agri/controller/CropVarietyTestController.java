package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateCropVarietyTestRequest;
import com.erp.platform.modules.agri.dto.CropVarietyTestDto;
import com.erp.platform.modules.agri.service.CropVarietyTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/crop-variety-tests")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CropVarietyTestController {

    private final CropVarietyTestService cropVarietyTestService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CropVarietyTestDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(cropVarietyTestService.list(pageable)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CropVarietyTestDto>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(cropVarietyTestService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CropVarietyTestDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cropVarietyTestService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CropVarietyTestDto>> create(@Valid @RequestBody CreateCropVarietyTestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cropVarietyTestService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CropVarietyTestDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCropVarietyTestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cropVarietyTestService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        cropVarietyTestService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
