package com.erp.platform.modules.fieldiot.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.fieldiot.dto.FieldIotDtos.*;
import com.erp.platform.modules.fieldiot.entity.FieldReading.ReadingKind;
import com.erp.platform.modules.fieldiot.service.FieldIotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/field-iot")
@RequiredArgsConstructor
@Tag(name = "Field IoT", description = "Satellite, weather and soil sensor data for mapped fields")
public class FieldIotController {

    private final FieldIotService service;

    // ── Dashboard & sync ─────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Field intelligence dashboard — latest readings and alerts per field")
    public ResponseEntity<ApiResponse<DashboardDto>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(service.dashboard()));
    }

    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pull a fresh weather, soil and satellite observation for every active field")
    public ResponseEntity<ApiResponse<SyncResultDto>> sync() {
        SyncResultDto result = service.sync();
        String msg = result.getFieldsSynced() == 0
                ? "Nothing to sync"
                : "Synced " + result.getFieldsSynced() + " field(s)"
                        + (result.isSimulated() ? " — simulated data" : " — live provider data");
        return ResponseEntity.ok(ApiResponse.success(result, msg));
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    @GetMapping("/fields")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List mapped fields")
    public ResponseEntity<ApiResponse<PageResponse<FieldPlotDto>>> listFields(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listFields(PageRequest.of(page, size))));
    }

    @GetMapping("/fields/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a mapped field")
    public ResponseEntity<ApiResponse<FieldPlotDto>> getField(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getField(id)));
    }

    @PostMapping("/fields")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Map a field")
    public ResponseEntity<ApiResponse<FieldPlotDto>> createField(@RequestBody FieldPlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createField(request), "Field mapped"));
    }

    @PutMapping("/fields/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a mapped field")
    public ResponseEntity<ApiResponse<FieldPlotDto>> updateField(
            @PathVariable UUID id, @RequestBody FieldPlotRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateField(id, request), "Field updated"));
    }

    @DeleteMapping("/fields/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    @Operation(summary = "Remove a mapped field")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable UUID id) {
        service.deleteField(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Field removed"));
    }

    // ── Devices ──────────────────────────────────────────────────────────────

    @GetMapping("/devices")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List registered IoT devices")
    public ResponseEntity<ApiResponse<PageResponse<IotDeviceDto>>> listDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listDevices(PageRequest.of(page, size))));
    }

    @PostMapping("/devices")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register an IoT device")
    public ResponseEntity<ApiResponse<IotDeviceDto>> createDevice(@RequestBody IotDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createDevice(request), "Device registered"));
    }

    @PutMapping("/devices/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an IoT device")
    public ResponseEntity<ApiResponse<IotDeviceDto>> updateDevice(
            @PathVariable UUID id, @RequestBody IotDeviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateDevice(id, request), "Device updated"));
    }

    @DeleteMapping("/devices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN', 'MANAGER')")
    @Operation(summary = "Remove an IoT device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable UUID id) {
        service.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Device removed"));
    }

    // ── Readings ─────────────────────────────────────────────────────────────

    @GetMapping("/readings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List observations, newest first")
    public ResponseEntity<ApiResponse<PageResponse<FieldReadingDto>>> listReadings(
            @RequestParam(required = false) UUID fieldPlotId,
            @RequestParam(required = false) ReadingKind kind,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listReadings(fieldPlotId, kind, PageRequest.of(page, size))));
    }

    @PostMapping("/ingest")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept a reading pushed by a field device")
    public ResponseEntity<ApiResponse<FieldReadingDto>> ingest(@RequestBody IngestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.ingest(request), "Reading recorded"));
    }
}
