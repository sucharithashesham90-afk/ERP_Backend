package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.BillOfMaterials;
import com.erp.platform.modules.manufacturing.entity.ProcessOrder;
import com.erp.platform.modules.manufacturing.entity.ProcessingLine;
import com.erp.platform.modules.manufacturing.entity.ProductionJob;
import com.erp.platform.modules.manufacturing.entity.ProductionJob.JobStatus;
import com.erp.platform.modules.manufacturing.entity.QualityCheck;
import com.erp.platform.modules.manufacturing.entity.WorkOrder;
import com.erp.platform.modules.manufacturing.entity.WorkOrder.WorkOrderStatus;
import com.erp.platform.modules.manufacturing.repository.ProcessingLineRepository;
import com.erp.platform.modules.manufacturing.service.ManufacturingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Manufacturing", description = "Work orders and BOM management")
public class ManufacturingController {

    private final ManufacturingService manufacturingService;
    private final ProcessingLineRepository processingLineRepo;
    private final TenantContext tenantContext;

    // ---- Work Orders ----

    @GetMapping("/api/v1/manufacturing/work-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List work orders")
    public ResponseEntity<ApiResponse<PageResponse<WorkOrder>>> listWorkOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) WorkOrderStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.listWorkOrders(status, pageable)));
    }

    @GetMapping("/api/v1/manufacturing/work-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get work order by ID")
    public ResponseEntity<ApiResponse<WorkOrder>> getWorkOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getWorkOrderById(id)));
    }

    @PostMapping("/api/v1/manufacturing/work-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create work order")
    public ResponseEntity<ApiResponse<WorkOrder>> createWorkOrder(@RequestBody WorkOrder workOrder) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(manufacturingService.createWorkOrder(workOrder), "Work order created"));
    }

    @PatchMapping("/api/v1/manufacturing/work-orders/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update work order status")
    public ResponseEntity<ApiResponse<WorkOrder>> updateWorkOrderStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        WorkOrderStatus status = WorkOrderStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateWorkOrderStatus(id, status)));
    }

    @PutMapping("/api/v1/manufacturing/work-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update work order (DRAFT only)")
    public ResponseEntity<ApiResponse<WorkOrder>> updateWorkOrder(
            @PathVariable UUID id,
            @RequestBody WorkOrder workOrder) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateWorkOrder(id, workOrder), "Work order updated"));
    }

    @DeleteMapping("/api/v1/manufacturing/work-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete work order (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(@PathVariable UUID id) {
        manufacturingService.deleteWorkOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Work order deleted"));
    }

    // ---- BOM ----

    @GetMapping("/api/v1/manufacturing/bom")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bill of materials")
    public ResponseEntity<ApiResponse<PageResponse<BillOfMaterials>>> listBOMs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.listBOMs(pageable)));
    }

    @GetMapping("/api/v1/manufacturing/bom/by-product/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get BOMs by product ID")
    public ResponseEntity<ApiResponse<List<BillOfMaterials>>> getBOMsByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getBOMsByProduct(productId)));
    }

    @GetMapping("/api/v1/manufacturing/bom/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get BOM by ID")
    public ResponseEntity<ApiResponse<BillOfMaterials>> getBOM(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getBOMById(id)));
    }

    @PostMapping("/api/v1/manufacturing/bom")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create BOM")
    public ResponseEntity<ApiResponse<BillOfMaterials>> createBOM(@RequestBody BillOfMaterials bom) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(manufacturingService.createBOM(bom), "BOM created"));
    }

    @PutMapping("/api/v1/manufacturing/bom/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update BOM")
    public ResponseEntity<ApiResponse<BillOfMaterials>> updateBOM(
            @PathVariable UUID id,
            @RequestBody BillOfMaterials bom) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateBOM(id, bom), "BOM updated"));
    }

    @DeleteMapping("/api/v1/manufacturing/bom/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete BOM (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteBOM(@PathVariable UUID id) {
        manufacturingService.deleteBOM(id);
        return ResponseEntity.ok(ApiResponse.success(null, "BOM deleted"));
    }

    // ---- Process Orders ----

    @GetMapping("/api/v1/manufacturing/process-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process orders")
    public ResponseEntity<ApiResponse<PageResponse<ProcessOrder>>> listProcessOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.listProcessOrders(status, pageable)));
    }

    @PostMapping("/api/v1/manufacturing/process-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process order")
    public ResponseEntity<ApiResponse<ProcessOrder>> createProcessOrder(@RequestBody ProcessOrder po) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(manufacturingService.createProcessOrder(po), "Process order created"));
    }

    @PutMapping("/api/v1/manufacturing/process-orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process order")
    public ResponseEntity<ApiResponse<ProcessOrder>> updateProcessOrder(
            @PathVariable UUID id, @RequestBody ProcessOrder po) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateProcessOrder(id, po)));
    }

    @PatchMapping("/api/v1/manufacturing/process-orders/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process order status")
    public ResponseEntity<ApiResponse<ProcessOrder>> updateProcessOrderStatus(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.updateProcessOrderStatus(id, body.get("status"))));
    }

    // ---- Quality Checks ----

    @GetMapping("/api/v1/manufacturing/quality-checks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List quality checks")
    public ResponseEntity<ApiResponse<PageResponse<QualityCheck>>> listQualityChecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String result) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.listQualityChecks(result, pageable)));
    }

    @PostMapping("/api/v1/manufacturing/quality-checks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create quality check")
    public ResponseEntity<ApiResponse<QualityCheck>> createQualityCheck(@RequestBody QualityCheck qc) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(manufacturingService.createQualityCheck(qc), "Quality check created"));
    }

    @PutMapping("/api/v1/manufacturing/quality-checks/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quality check")
    public ResponseEntity<ApiResponse<QualityCheck>> updateQualityCheck(
            @PathVariable UUID id, @RequestBody QualityCheck qc) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateQualityCheck(id, qc)));
    }

    // ---- Production Jobs ----

    @GetMapping("/api/v1/manufacturing/production-jobs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List / search production jobs")
    public ResponseEntity<ApiResponse<PageResponse<ProductionJob>>> listProductionJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String processType,
            @RequestParam(required = false) String jobNumber,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) String workCenter,
            @RequestParam(required = false) String storageLocation,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startTo,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endTo) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.listProductionJobs(status, processType,
                        jobNumber, locationName, workCenter, storageLocation, lotNumber,
                        startFrom, startTo, endFrom, endTo, pageable)));
    }

    @GetMapping("/api/v1/manufacturing/production-jobs/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get production job by ID")
    public ResponseEntity<ApiResponse<ProductionJob>> getProductionJob(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.getProductionJobById(id)));
    }

    @PostMapping("/api/v1/manufacturing/production-jobs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production job")
    public ResponseEntity<ApiResponse<ProductionJob>> createProductionJob(@RequestBody ProductionJob job) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(manufacturingService.createProductionJob(job), "Production job created"));
    }

    @PutMapping("/api/v1/manufacturing/production-jobs/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production job")
    public ResponseEntity<ApiResponse<ProductionJob>> updateProductionJob(
            @PathVariable UUID id, @RequestBody ProductionJob job) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingService.updateProductionJob(id, job)));
    }

    @PatchMapping("/api/v1/manufacturing/production-jobs/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production job status")
    public ResponseEntity<ApiResponse<ProductionJob>> updateProductionJobStatus(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        JobStatus status = JobStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(
                manufacturingService.updateProductionJobStatus(id, status)));
    }

    @DeleteMapping("/api/v1/manufacturing/production-jobs/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete production job")
    public ResponseEntity<ApiResponse<Void>> deleteProductionJob(@PathVariable UUID id) {
        manufacturingService.deleteProductionJob(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Production job deleted"));
    }

    // ---- Processing Lines ----

    @GetMapping("/api/v1/manufacturing/processing-lines")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List processing lines (paged)")
    public ResponseEntity<ApiResponse<PageResponse<ProcessingLine>>> listProcessingLines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(processingLineRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable))));
    }

    @GetMapping("/api/v1/manufacturing/processing-lines/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active processing lines (unpaged, for dropdowns)")
    public ResponseEntity<ApiResponse<List<ProcessingLine>>> listActiveProcessingLines() {
        return ResponseEntity.ok(ApiResponse.success(
                processingLineRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())));
    }

    @PostMapping("/api/v1/manufacturing/processing-lines")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create processing line")
    public ResponseEntity<ApiResponse<ProcessingLine>> createProcessingLine(@RequestBody ProcessingLine body) {
        UUID tenantId = tenantContext.current();
        if (processingLineRepo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, body.getCode()))
            throw AppException.badRequest("Processing line code already exists: " + body.getCode());
        body.setTenantId(tenantId);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(processingLineRepo.save(body)));
    }

    @PutMapping("/api/v1/manufacturing/processing-lines/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update processing line")
    public ResponseEntity<ApiResponse<ProcessingLine>> updateProcessingLine(
            @PathVariable UUID id, @RequestBody ProcessingLine body) {
        UUID tenantId = tenantContext.current();
        ProcessingLine line = processingLineRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Processing line not found: " + id));
        if (body.getName() != null) line.setName(body.getName());
        if (body.getLineType() != null) line.setLineType(body.getLineType());
        if (body.getWarehouseId() != null) line.setWarehouseId(body.getWarehouseId());
        if (body.getWarehouseName() != null) line.setWarehouseName(body.getWarehouseName());
        if (body.getLocationId() != null) line.setLocationId(body.getLocationId());
        if (body.getLocationName() != null) line.setLocationName(body.getLocationName());
        if (body.getConsumption() != null) line.setConsumption(body.getConsumption());
        if (body.getUsageHoursPerDay() != null) line.setUsageHoursPerDay(body.getUsageHoursPerDay());
        if (body.getCapacity() != null) line.setCapacity(body.getCapacity());
        if (body.getCapacityUnit() != null) line.setCapacityUnit(body.getCapacityUnit());
        if (body.getDescription() != null) line.setDescription(body.getDescription());
        // Process step and ownership are on the form too; leaving them out meant an edit reverted
        // whatever the user had just picked.
        if (body.getProcessStepId() != null) line.setProcessStepId(body.getProcessStepId());
        if (body.getProcessStepName() != null) line.setProcessStepName(body.getProcessStepName());
        if (body.getOwnership() != null) line.setOwnership(body.getOwnership());
        line.setActive(body.isActive());
        return ResponseEntity.ok(ApiResponse.success(processingLineRepo.save(line)));
    }

    @DeleteMapping("/api/v1/manufacturing/processing-lines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete processing line")
    public ResponseEntity<ApiResponse<Void>> deleteProcessingLine(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessingLine line = processingLineRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Processing line not found: " + id));
        line.setDeletedAt(java.time.LocalDateTime.now());
        processingLineRepo.save(line);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
