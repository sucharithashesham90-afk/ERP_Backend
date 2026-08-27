package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeasonPeriodRequest;
import com.erp.platform.modules.agri.dto.SeasonPeriodDto;
import com.erp.platform.modules.agri.service.SeasonPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/season-periods")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeasonPeriodController {

    private final SeasonPeriodService seasonPeriodService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeasonPeriodDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String periodType) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(seasonPeriodService.list(periodType, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonPeriodDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seasonPeriodService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeasonPeriodDto>> create(@Valid @RequestBody CreateSeasonPeriodRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seasonPeriodService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonPeriodDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSeasonPeriodRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seasonPeriodService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seasonPeriodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
