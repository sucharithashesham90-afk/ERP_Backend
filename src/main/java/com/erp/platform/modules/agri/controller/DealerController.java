package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateDealerRequest;
import com.erp.platform.modules.agri.dto.DealerDto;
import com.erp.platform.modules.agri.service.DealerService;
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
@RequestMapping("/api/v1/agri/dealers")
@RequiredArgsConstructor
@Tag(name = "Dealers", description = "Distributor/dealer network management")
public class DealerController {

    private final DealerService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List dealers")
    public ResponseEntity<ApiResponse<PageResponse<DealerDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get dealer by ID")
    public ResponseEntity<ApiResponse<DealerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create dealer")
    public ResponseEntity<ApiResponse<DealerDto>> create(@Valid @RequestBody CreateDealerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Dealer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update dealer")
    public ResponseEntity<ApiResponse<DealerDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateDealerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete dealer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dealer deleted"));
    }
}
