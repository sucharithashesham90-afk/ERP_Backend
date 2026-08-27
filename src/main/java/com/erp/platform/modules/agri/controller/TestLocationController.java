package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateTestLocationRequest;
import com.erp.platform.modules.agri.dto.TestLocationDto;
import com.erp.platform.modules.agri.service.TestLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/test-locations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TestLocationController {

    private final TestLocationService testLocationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TestLocationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(testLocationService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestLocationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testLocationService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TestLocationDto>> create(@Valid @RequestBody CreateTestLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(testLocationService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TestLocationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTestLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(testLocationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        testLocationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
