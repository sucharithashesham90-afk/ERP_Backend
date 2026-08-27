package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProducerAdvanceRequest;
import com.erp.platform.modules.agri.dto.ProducerAdvanceDto;
import com.erp.platform.modules.agri.service.ProducerAdvanceService;
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
@RequestMapping("/api/v1/agri/producer-advances")
@RequiredArgsConstructor
@Tag(name = "Producer Advances", description = "Field producer advance payment management")
public class ProducerAdvanceController {

    private final ProducerAdvanceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List producer advances")
    public ResponseEntity<ApiResponse<PageResponse<ProducerAdvanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("advanceDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get producer advance by ID")
    public ResponseEntity<ApiResponse<ProducerAdvanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create producer advance")
    public ResponseEntity<ApiResponse<ProducerAdvanceDto>> create(@Valid @RequestBody CreateProducerAdvanceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Producer advance created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update producer advance")
    public ResponseEntity<ApiResponse<ProducerAdvanceDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateProducerAdvanceRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete producer advance")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Producer advance deleted"));
    }
}
