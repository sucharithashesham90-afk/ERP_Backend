package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.ByproductType;
import com.erp.platform.modules.agri.entity.WasteType;
import com.erp.platform.modules.agri.repository.ByproductTypeRepository;
import com.erp.platform.modules.agri.repository.WasteTypeRepository;
import com.erp.platform.modules.manufacturing.entity.OperationType;
import com.erp.platform.modules.manufacturing.entity.ProcessStep;
import com.erp.platform.modules.manufacturing.entity.ProcessTemplate;
import com.erp.platform.modules.manufacturing.entity.ProcessTemplateStep;
import com.erp.platform.modules.manufacturing.entity.ProcessTreatment;
import com.erp.platform.modules.manufacturing.repository.ProcessTreatmentRepository;
import com.erp.platform.modules.manufacturing.service.ProcessStepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Process Steps", description = "Process step and template management")
public class ProcessStepController {

    private final ProcessStepService service;
    private final ProcessTreatmentRepository processTreatmentRepo;
    private final ByproductTypeRepository byproductTypeRepo;
    private final WasteTypeRepository wasteTypeRepo;
    private final TenantContext tenantContext;

    // ---- Operation Types ----

    @GetMapping("/api/v1/manufacturing/operation-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List operation types")
    public ResponseEntity<ApiResponse<PageResponse<OperationType>>> listOperationTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listOperationTypes(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/manufacturing/operation-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create operation type")
    public ResponseEntity<ApiResponse<OperationType>> createOperationType(@RequestBody OperationType req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createOperationType(req), "Created"));
    }

    @PutMapping("/api/v1/manufacturing/operation-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update operation type")
    public ResponseEntity<ApiResponse<OperationType>> updateOperationType(@PathVariable UUID id, @RequestBody OperationType req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOperationType(id, req)));
    }

    @DeleteMapping("/api/v1/manufacturing/operation-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete operation type")
    public ResponseEntity<ApiResponse<Void>> deleteOperationType(@PathVariable UUID id) {
        service.deleteOperationType(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Process Steps ----

    @GetMapping("/api/v1/manufacturing/process-steps")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process steps")
    public ResponseEntity<ApiResponse<PageResponse<ProcessStep>>> listSteps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listSteps(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/manufacturing/process-steps/all-active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active process steps for dropdown")
    public ResponseEntity<ApiResponse<List<ProcessStep>>> getAllActiveSteps() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActiveSteps()));
    }

    @PostMapping("/api/v1/manufacturing/process-steps")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process step")
    public ResponseEntity<ApiResponse<ProcessStep>> createStep(@RequestBody ProcessStep req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createStep(req), "Created"));
    }

    @PutMapping("/api/v1/manufacturing/process-steps/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process step")
    public ResponseEntity<ApiResponse<ProcessStep>> updateStep(@PathVariable UUID id, @RequestBody ProcessStep req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateStep(id, req)));
    }

    @DeleteMapping("/api/v1/manufacturing/process-steps/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete process step")
    public ResponseEntity<ApiResponse<Void>> deleteStep(@PathVariable UUID id) {
        service.deleteStep(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Process Templates ----

    @GetMapping("/api/v1/manufacturing/process-templates")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process templates")
    public ResponseEntity<ApiResponse<PageResponse<ProcessTemplate>>> listTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listTemplates(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/manufacturing/process-templates")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process template")
    public ResponseEntity<ApiResponse<ProcessTemplate>> createTemplate(@RequestBody ProcessTemplate req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createTemplate(req), "Created"));
    }

    @PutMapping("/api/v1/manufacturing/process-templates/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process template")
    public ResponseEntity<ApiResponse<ProcessTemplate>> updateTemplate(@PathVariable UUID id, @RequestBody ProcessTemplate req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTemplate(id, req)));
    }

    @DeleteMapping("/api/v1/manufacturing/process-templates/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete process template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Template Steps ----

    @GetMapping("/api/v1/manufacturing/process-templates/{id}/steps")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get steps for a process template")
    public ResponseEntity<ApiResponse<List<ProcessTemplateStep>>> getTemplateSteps(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTemplateSteps(id)));
    }

    @PostMapping("/api/v1/manufacturing/process-templates/{id}/steps")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add step to process template")
    public ResponseEntity<ApiResponse<ProcessTemplateStep>> addStepToTemplate(
            @PathVariable UUID id, @RequestBody ProcessTemplateStep req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(service.addStepToTemplate(id, req), "Step added"));
    }

    @DeleteMapping("/api/v1/manufacturing/process-templates/steps/{stepId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove step from process template")
    public ResponseEntity<ApiResponse<Void>> removeStepFromTemplate(@PathVariable UUID stepId) {
        service.removeStepFromTemplate(stepId);
        return ResponseEntity.ok(ApiResponse.success(null, "Step removed"));
    }

    // ---- Treatments ----

    @GetMapping("/api/v1/manufacturing/treatments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List treatments")
    public ResponseEntity<ApiResponse<PageResponse<ProcessTreatment>>> listTreatments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(processTreatmentRepo.findByTenantIdAndDeletedAtIsNull(
                        tenantContext.current(), PageRequest.of(page, size, Sort.by("name"))))));
    }

    @GetMapping("/api/v1/manufacturing/treatments/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active treatments for dropdown")
    public ResponseEntity<ApiResponse<List<ProcessTreatment>>> listActiveTreatments() {
        return ResponseEntity.ok(ApiResponse.success(
                processTreatmentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())));
    }

    @PostMapping("/api/v1/manufacturing/treatments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create ProcessTreatment")
    public ResponseEntity<ApiResponse<ProcessTreatment>> createTreatment(@RequestBody ProcessTreatment req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(processTreatmentRepo.save(req)));
    }

    @PutMapping("/api/v1/manufacturing/treatments/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update ProcessTreatment")
    public ResponseEntity<ApiResponse<ProcessTreatment>> updateTreatment(@PathVariable UUID id, @RequestBody ProcessTreatment req) {
        ProcessTreatment e = processTreatmentRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProcessTreatment not found: " + id));
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(processTreatmentRepo.save(e)));
    }

    @DeleteMapping("/api/v1/manufacturing/treatments/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete ProcessTreatment")
    public ResponseEntity<ApiResponse<Void>> deleteTreatment(@PathVariable UUID id) {
        ProcessTreatment e = processTreatmentRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProcessTreatment not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        processTreatmentRepo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ---- Byproduct Types ----

    @GetMapping("/api/v1/manufacturing/byproduct-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List byproduct types")
    public ResponseEntity<ApiResponse<PageResponse<ByproductType>>> listByproductTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(byproductTypeRepo.findByTenantIdAndDeletedAtIsNull(
                        tenantContext.current(), PageRequest.of(page, size, Sort.by("name"))))));
    }

    @GetMapping("/api/v1/manufacturing/byproduct-types/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active byproduct types for dropdown")
    public ResponseEntity<ApiResponse<List<ByproductType>>> listActiveByproductTypes() {
        return ResponseEntity.ok(ApiResponse.success(
                byproductTypeRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())));
    }

    @PostMapping("/api/v1/manufacturing/byproduct-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create byproduct type")
    public ResponseEntity<ApiResponse<ByproductType>> createByproductType(@RequestBody ByproductType req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(byproductTypeRepo.save(req)));
    }

    @PutMapping("/api/v1/manufacturing/byproduct-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update byproduct type")
    public ResponseEntity<ApiResponse<ByproductType>> updateByproductType(@PathVariable UUID id, @RequestBody ByproductType req) {
        ByproductType e = byproductTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Byproduct type not found: " + id));
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(byproductTypeRepo.save(e)));
    }

    @DeleteMapping("/api/v1/manufacturing/byproduct-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete byproduct type")
    public ResponseEntity<ApiResponse<Void>> deleteByproductType(@PathVariable UUID id) {
        ByproductType e = byproductTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Byproduct type not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        byproductTypeRepo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ---- Waste Types ----

    @GetMapping("/api/v1/manufacturing/waste-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List waste types")
    public ResponseEntity<ApiResponse<PageResponse<WasteType>>> listWasteTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(wasteTypeRepo.findByTenantIdAndDeletedAtIsNull(
                        tenantContext.current(), PageRequest.of(page, size, Sort.by("name"))))));
    }

    @GetMapping("/api/v1/manufacturing/waste-types/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active waste types for dropdown")
    public ResponseEntity<ApiResponse<List<WasteType>>> listActiveWasteTypes() {
        return ResponseEntity.ok(ApiResponse.success(
                wasteTypeRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())));
    }

    @PostMapping("/api/v1/manufacturing/waste-types")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create waste type")
    public ResponseEntity<ApiResponse<WasteType>> createWasteType(@RequestBody WasteType req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(wasteTypeRepo.save(req)));
    }

    @PutMapping("/api/v1/manufacturing/waste-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update waste type")
    public ResponseEntity<ApiResponse<WasteType>> updateWasteType(@PathVariable UUID id, @RequestBody WasteType req) {
        WasteType e = wasteTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Waste type not found: " + id));
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(wasteTypeRepo.save(e)));
    }

    @DeleteMapping("/api/v1/manufacturing/waste-types/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete waste type")
    public ResponseEntity<ApiResponse<Void>> deleteWasteType(@PathVariable UUID id) {
        WasteType e = wasteTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Waste type not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        wasteTypeRepo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

