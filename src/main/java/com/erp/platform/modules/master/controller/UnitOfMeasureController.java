package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateUoMRequest;
import com.erp.platform.modules.master.dto.UnitOfMeasureDto;
import com.erp.platform.modules.master.entity.UnitOfMeasure.UoMType;
import com.erp.platform.modules.master.service.UnitOfMeasureService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master/uom")
@RequiredArgsConstructor
@Tag(name = "Unit of Measure", description = "UoM master data management")
public class UnitOfMeasureController {

    private final UnitOfMeasureService uomService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List units of measure")
    public ResponseEntity<ApiResponse<PageResponse<UnitOfMeasureDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size,
            @RequestParam(required = false) UoMType type,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(uomService.list(type, pageable)));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all units of measure")
    public ResponseEntity<ApiResponse<List<UnitOfMeasureDto>>> all() {
        return ResponseEntity.ok(ApiResponse.success(uomService.all()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get UoM by ID")
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(uomService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new UoM")
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> create(@Valid @RequestBody CreateUoMRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(uomService.create(request), "Unit of Measure created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update UoM")
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUoMRequest request) {
        return ResponseEntity.ok(ApiResponse.success(uomService.update(id, request), "Unit of Measure updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete UoM (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        uomService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Unit of Measure deleted successfully"));
    }

    @GetMapping("/convert")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Convert quantity between UoMs")
    public ResponseEntity<ApiResponse<BigDecimal>> convert(
            @RequestParam UUID fromUomId,
            @RequestParam UUID toUomId,
            @RequestParam BigDecimal qty) {
        return ResponseEntity.ok(ApiResponse.success(uomService.convert(fromUomId, toUomId, qty)));
    }
}
