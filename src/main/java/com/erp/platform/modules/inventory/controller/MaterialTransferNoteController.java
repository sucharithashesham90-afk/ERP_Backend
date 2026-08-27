package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateMaterialTransferNoteRequest;
import com.erp.platform.modules.inventory.dto.MaterialTransferNoteDto;
import com.erp.platform.modules.inventory.service.MaterialTransferNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/material-transfers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MaterialTransferNoteController {

    private final MaterialTransferNoteService materialTransferNoteService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MaterialTransferNoteDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(materialTransferNoteService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialTransferNoteDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(materialTransferNoteService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaterialTransferNoteDto>> create(@Valid @RequestBody CreateMaterialTransferNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialTransferNoteService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialTransferNoteDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMaterialTransferNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(materialTransferNoteService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        materialTransferNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
