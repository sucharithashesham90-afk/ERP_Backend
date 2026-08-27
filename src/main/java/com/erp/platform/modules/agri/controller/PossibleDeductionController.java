package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePossibleDeductionRequest;
import com.erp.platform.modules.agri.dto.PossibleDeductionDto;
import com.erp.platform.modules.agri.service.PossibleDeductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/possible-deductions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PossibleDeductionController {

    private final PossibleDeductionService possibleDeductionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PossibleDeductionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(possibleDeductionService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PossibleDeductionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(possibleDeductionService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PossibleDeductionDto>> create(@Valid @RequestBody CreatePossibleDeductionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(possibleDeductionService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PossibleDeductionDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePossibleDeductionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(possibleDeductionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        possibleDeductionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
