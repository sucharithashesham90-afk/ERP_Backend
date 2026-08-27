package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessBomRequest;
import com.erp.platform.modules.agri.dto.ProcessBomDto;
import com.erp.platform.modules.agri.service.ProcessBomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/process-boms")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProcessBomController {

    private final ProcessBomService processBomService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProcessBomDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(processBomService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessBomDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(processBomService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessBomDto>> create(@Valid @RequestBody CreateProcessBomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processBomService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessBomDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProcessBomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processBomService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        processBomService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
