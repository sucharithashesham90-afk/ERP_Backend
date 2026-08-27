package com.erp.platform.modules.intake.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.intake.dto.CreateTruckWiseIntakeRequest;
import com.erp.platform.modules.intake.dto.TruckWiseIntakeDto;
import com.erp.platform.modules.intake.service.TruckWiseIntakeService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intake/truck-wise-intake")
@RequiredArgsConstructor
@Tag(name = "Truck Wise Intake", description = "Truck-wise intake management")
public class TruckWiseIntakeController {

    private final TruckWiseIntakeService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List truck-wise intake records")
    public ResponseEntity<ApiResponse<PageResponse<TruckWiseIntakeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("intakeDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get truck-wise intake by ID")
    public ResponseEntity<ApiResponse<TruckWiseIntakeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create truck-wise intake")
    public ResponseEntity<ApiResponse<TruckWiseIntakeDto>> create(@Valid @RequestBody CreateTruckWiseIntakeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Truck-wise intake created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update truck-wise intake")
    public ResponseEntity<ApiResponse<TruckWiseIntakeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTruckWiseIntakeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete truck-wise intake")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Truck-wise intake deleted"));
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Save line items for a truck-wise intake")
    public ResponseEntity<ApiResponse<TruckWiseIntakeDto>> saveLines(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(service.saveLines(id, body.get("lines")), "Intake lines saved"));
    }
}
