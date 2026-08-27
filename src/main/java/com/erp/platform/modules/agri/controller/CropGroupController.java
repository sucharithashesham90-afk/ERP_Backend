package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateCropGroupRequest;
import com.erp.platform.modules.agri.dto.CropGroupDto;
import com.erp.platform.modules.agri.service.CropGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/crop-groups")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CropGroupController {

    private final CropGroupService cropGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CropGroupDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(cropGroupService.list(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CropGroupDto>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(cropGroupService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CropGroupDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cropGroupService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CropGroupDto>> create(@Valid @RequestBody CreateCropGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cropGroupService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CropGroupDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateCropGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cropGroupService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        cropGroupService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
