package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.manufacturing.entity.BillOfMaterials;
import com.erp.platform.modules.manufacturing.entity.ProcessJobInputLot;
import com.erp.platform.modules.manufacturing.entity.ProcessJobOutputLine;
import com.erp.platform.modules.manufacturing.entity.ProcessOrder;
import com.erp.platform.modules.manufacturing.entity.ProductionJob;
import com.erp.platform.modules.manufacturing.entity.ProductionJob.JobStatus;
import com.erp.platform.modules.manufacturing.entity.QualityCheck;
import com.erp.platform.modules.manufacturing.entity.WorkOrder;
import com.erp.platform.modules.manufacturing.entity.WorkOrder.WorkOrderStatus;
import com.erp.platform.modules.inventory.entity.LotMovement;
import com.erp.platform.modules.inventory.entity.LotMovement.LotMovType;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.entity.LotStock.LotSource;
import com.erp.platform.modules.inventory.entity.LotStock.LotStockStatus;
import com.erp.platform.modules.inventory.entity.StorageLocation;
import com.erp.platform.modules.inventory.repository.LotMovementRepository;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import com.erp.platform.modules.inventory.repository.StorageLocationRepository;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.entity.SeedConversion;
import com.erp.platform.modules.inventory.entity.InventoryIssue;
import com.erp.platform.modules.inventory.entity.InventoryReceipt;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.inventory.repository.SeedConversionRepository;
import com.erp.platform.modules.inventory.repository.InventoryIssueRepository;
import com.erp.platform.modules.inventory.repository.InventoryReceiptRepository;
import com.erp.platform.modules.agri.entity.LotIssueDetail;
import com.erp.platform.modules.agri.repository.LotIssueDetailRepository;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.manufacturing.repository.BOMRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessOrderRepository;
import com.erp.platform.modules.manufacturing.repository.ProductionJobRepository;
import com.erp.platform.modules.manufacturing.repository.QualityCheckRepository;
import com.erp.platform.modules.manufacturing.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ManufacturingService {

    private final WorkOrderRepository workOrderRepository;
    private final BOMRepository bomRepository;
    private final ProcessOrderRepository processOrderRepository;
    private final QualityCheckRepository qualityCheckRepository;
    private final ProductionJobRepository productionJobRepository;
    private final LotStockRepository lotStockRepository;
    private final LotMovementRepository lotMovementRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final StockLotRepository stockLotRepository;
    private final SeedConversionRepository seedConversionRepository;
    private final InventoryIssueRepository inventoryIssueRepository;
    private final InventoryReceiptRepository inventoryReceiptRepository;
    private final LotIssueDetailRepository lotIssueDetailRepository;
    private final GodownRepository godownRepository;
    private final StockService stockService;
    private final TenantContext tenantContext;
    private final UserContext userContext;

    // --- Work Orders ---

    public PageResponse<WorkOrder> listWorkOrders(WorkOrderStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<String> locs = userContext.getAllowedLocations();
        var page = userContext.hasLocationRestriction()
                ? (status != null
                        ? workOrderRepository.findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(tenantId, status, locs, pageable)
                        : workOrderRepository.findByTenantIdAndLocationsAndDeletedAtIsNull(tenantId, locs, pageable))
                : (status != null
                        ? workOrderRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                        : workOrderRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
        return PageResponse.of(page);
    }

    public WorkOrder getWorkOrderById(UUID id) {
        return findWorkOrderOrThrow(id);
    }

    @Transactional
    public WorkOrder createWorkOrder(WorkOrder workOrder) {
        UUID tenantId = tenantContext.current();
        workOrder.setTenantId(tenantId);
        if (workOrder.getOrderNumber() == null) {
            workOrder.setOrderNumber("WO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        if (workOrder.getStatus() == null) workOrder.setStatus(WorkOrderStatus.DRAFT);
        WorkOrder saved = workOrderRepository.save(workOrder);
        log.info("WorkOrder created: id={}, number={}", saved.getId(), saved.getOrderNumber());
        return saved;
    }

    @Transactional
    public WorkOrder updateWorkOrderStatus(UUID id, WorkOrderStatus status) {
        WorkOrder wo = findWorkOrderOrThrow(id);
        wo.setStatus(status);

        if (status == WorkOrderStatus.IN_PROGRESS && wo.getActualStartDate() == null) {
            wo.setActualStartDate(LocalDate.now());
            // Deduct BOM components from stock
            if (wo.getBomId() != null) {
                bomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), wo.getBomId())
                        .ifPresent(bom -> {
                            BigDecimal qty = wo.getPlannedQuantity() != null ? wo.getPlannedQuantity() : BigDecimal.ONE;
                            // Pre-flight: check all components are available before deducting any
                            for (var item : bom.getItems()) {
                                if (item.getMaterialId() == null || item.getQuantity() == null) continue;
                                BigDecimal compQty = item.getQuantity().multiply(qty);
                                BigDecimal available = stockService.getAvailableQuantity(item.getMaterialId());
                                if (available.compareTo(compQty) < 0) {
                                    throw AppException.badRequest(
                                            "Insufficient stock for BOM component. Required: " + compQty
                                            + ", Available: " + available + ". Cannot start work order " + wo.getOrderNumber());
                                }
                            }
                            // All components available — now deduct
                            for (var item : bom.getItems()) {
                                if (item.getMaterialId() == null || item.getQuantity() == null) continue;
                                BigDecimal compQty = item.getQuantity().multiply(qty);
                                stockService.deductStock(item.getMaterialId(), null, compQty,
                                        "WORK_ORDER", wo.getId(), wo.getOrderNumber(),
                                        "RAW", "GOOD", null, "PRODUCTION_CONSUME");
                                log.info("BOM component deducted: wo={}, material={}, qty={}",
                                        wo.getOrderNumber(), item.getMaterialId(), compQty);
                            }
                        });
            }
        }

        if (status == WorkOrderStatus.COMPLETED && wo.getActualEndDate() == null) {
            wo.setActualEndDate(LocalDate.now());
            // Add finished product output to stock
            BigDecimal produced = wo.getProducedQuantity() != null && wo.getProducedQuantity().compareTo(java.math.BigDecimal.ZERO) > 0
                    ? wo.getProducedQuantity() : wo.getPlannedQuantity();
            if (produced != null && produced.compareTo(java.math.BigDecimal.ZERO) > 0 && wo.getProductId() != null) {
                try {
                    stockService.addStock(wo.getProductId(), null, produced,
                            "WORK_ORDER", wo.getId(), wo.getOrderNumber(), wo.getProductName(),
                            "PROCESSED", "GOOD", null, "PRODUCTION");
                    log.info("Work order output added to stock: wo={}, product={}, qty={}",
                            wo.getOrderNumber(), wo.getProductId(), produced);
                } catch (Exception e) {
                    log.warn("Work order output stock addition failed for {}: {}", wo.getProductId(), e.getMessage());
                }
            }
        }

        if (status == WorkOrderStatus.CANCELLED && wo.getActualStartDate() != null && wo.getBomId() != null) {
            // Restore BOM components that were consumed when order went IN_PROGRESS
            bomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), wo.getBomId())
                    .ifPresent(bom -> {
                        BigDecimal qty = wo.getPlannedQuantity() != null ? wo.getPlannedQuantity() : BigDecimal.ONE;
                        for (var item : bom.getItems()) {
                            if (item.getMaterialId() == null) continue;
                            BigDecimal compQty = item.getQuantity().multiply(qty);
                            try {
                                stockService.addStock(item.getMaterialId(), null, compQty,
                                        "WORK_ORDER_CANCEL", wo.getId(), wo.getOrderNumber(), null,
                                        "RAW", "GOOD", null, "PRODUCTION_RESTORE");
                                log.info("BOM stock restored on WO cancel: wo={}, material={}, qty={}",
                                        wo.getOrderNumber(), item.getMaterialId(), compQty);
                            } catch (Exception e) {
                                log.warn("BOM stock restore skipped for {}: {}", item.getMaterialId(), e.getMessage());
                            }
                        }
                    });
        }

        return workOrderRepository.save(wo);
    }

    @Transactional
    public WorkOrder updateWorkOrder(UUID id, WorkOrder updated) {
        WorkOrder existing = findWorkOrderOrThrow(id);
        if (existing.getStatus() != WorkOrderStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT work orders can be edited");
        }
        if (updated.getProductId() != null) existing.setProductId(updated.getProductId());
        if (updated.getProductName() != null) existing.setProductName(updated.getProductName());
        existing.setBomId(updated.getBomId());
        if (updated.getPlannedQuantity() != null) existing.setPlannedQuantity(updated.getPlannedQuantity());
        existing.setPlannedStartDate(updated.getPlannedStartDate());
        existing.setPlannedEndDate(updated.getPlannedEndDate());
        if (updated.getPriority() != null) existing.setPriority(updated.getPriority());
        existing.setNotes(updated.getNotes());
        WorkOrder saved = workOrderRepository.save(existing);
        log.info("WorkOrder updated: id={}", id);
        return saved;
    }

    @Transactional
    public void deleteWorkOrder(UUID id) {
        WorkOrder wo = findWorkOrderOrThrow(id);
        wo.setDeletedAt(LocalDateTime.now());
        workOrderRepository.save(wo);
        log.info("WorkOrder soft-deleted: id={}", id);
    }

    // --- BOM ---

    public PageResponse<BillOfMaterials> listBOMs(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(bomRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public List<BillOfMaterials> getBOMsByProduct(UUID productId) {
        UUID tenantId = tenantContext.current();
        return bomRepository.findByTenantIdAndProductIdAndActiveTrueAndDeletedAtIsNull(tenantId, productId);
    }

    public BillOfMaterials getBOMById(UUID id) {
        return findBOMOrThrow(id);
    }

    @Transactional
    public BillOfMaterials createBOM(BillOfMaterials bom) {
        UUID tenantId = tenantContext.current();
        if (bom.getProductId() == null) throw AppException.badRequest("Product is required for BOM");
        bom.setTenantId(tenantId);
        if (bom.getItems() != null) bom.getItems().forEach(item -> item.setBom(bom));
        BillOfMaterials saved = bomRepository.save(bom);
        log.info("BOM created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public BillOfMaterials updateBOM(UUID id, BillOfMaterials updated) {
        BillOfMaterials existing = findBOMOrThrow(id);
        existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getProductId() != null) existing.setProductId(updated.getProductId());
        if (updated.getProductName() != null) existing.setProductName(updated.getProductName());
        if (updated.getVersion() != null) existing.setVersion(updated.getVersion());
        existing.setActive(updated.isActive());
        if (updated.getItems() != null) {
            existing.getItems().clear();
            updated.getItems().forEach(item -> item.setBom(existing));
            existing.getItems().addAll(updated.getItems());
        }
        BillOfMaterials saved = bomRepository.save(existing);
        log.info("BOM updated: id={}", id);
        return saved;
    }

    @Transactional
    public void deleteBOM(UUID id) {
        BillOfMaterials bom = findBOMOrThrow(id);
        bom.setDeletedAt(LocalDateTime.now());
        bomRepository.save(bom);
        log.info("BOM soft-deleted: id={}", id);
    }

    private WorkOrder findWorkOrderOrThrow(UUID id) {
        return workOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Work order not found: " + id));
    }

    private BillOfMaterials findBOMOrThrow(UUID id) {
        return bomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("BOM not found: " + id));
    }

    // --- Process Orders ---

    public PageResponse<ProcessOrder> listProcessOrders(String status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<String> locs = userContext.getAllowedLocations();
        var page = userContext.hasLocationRestriction()
                ? (status != null
                        ? processOrderRepository.findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(tenantId, status, locs, pageable)
                        : processOrderRepository.findByTenantIdAndLocationsAndDeletedAtIsNull(tenantId, locs, pageable))
                : (status != null
                        ? processOrderRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                        : processOrderRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
        return PageResponse.of(page);
    }

    @Transactional
    public ProcessOrder createProcessOrder(ProcessOrder po) {
        UUID tenantId = tenantContext.current();
        po.setTenantId(tenantId);
        if (po.getProcessNumber() == null) {
            po.setProcessNumber("PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        if (po.getStatus() == null) po.setStatus("PENDING");
        ProcessOrder saved = processOrderRepository.save(po);
        log.info("ProcessOrder created: id={}, number={}", saved.getId(), saved.getProcessNumber());
        return saved;
    }

    @Transactional
    public ProcessOrder updateProcessOrder(UUID id, ProcessOrder updated) {
        ProcessOrder existing = processOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process order not found: " + id));
        existing.setProcessName(updated.getProcessName());
        existing.setProcessType(updated.getProcessType());
        existing.setWorkOrderId(updated.getWorkOrderId());
        existing.setInputMaterialId(updated.getInputMaterialId());
        existing.setOutputProductId(updated.getOutputProductId());
        existing.setInputQuantity(updated.getInputQuantity());
        existing.setOutputQuantity(updated.getOutputQuantity());
        existing.setUnit(updated.getUnit());
        existing.setScheduledDate(updated.getScheduledDate());
        existing.setAssignedTo(updated.getAssignedTo());
        existing.setMachine(updated.getMachine());
        existing.setNotes(updated.getNotes());
        return processOrderRepository.save(existing);
    }

    @Transactional
    public ProcessOrder updateProcessOrderStatus(UUID id, String status) {
        UUID tenantId = tenantContext.current();
        ProcessOrder po = processOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Process order not found: " + id));
        po.setStatus(status);

        if ("COMPLETED".equals(status)) {
            // Deduct input material from stock
            if (po.getInputMaterialId() != null
                    && po.getInputQuantity() != null
                    && po.getInputQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
                try {
                    stockService.deductStock(po.getInputMaterialId(), null, po.getInputQuantity(),
                            "PROCESS_ORDER", po.getId(), po.getProcessNumber(),
                            "RAW", "GOOD", null, "PRODUCTION_CONSUME");
                    log.info("Process order input deducted: po={}, material={}, qty={}",
                            po.getProcessNumber(), po.getInputMaterialId(), po.getInputQuantity());
                } catch (Exception e) {
                    log.warn("Process order input deduction skipped for {}: {}", po.getInputMaterialId(), e.getMessage());
                }
            }

            // Add output product to stock
            if (po.getOutputProductId() != null
                    && po.getOutputQuantity() != null
                    && po.getOutputQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
                try {
                    stockService.addStock(po.getOutputProductId(), null, po.getOutputQuantity(),
                            "PROCESS_ORDER", po.getId(), po.getProcessNumber(), po.getProcessName(),
                            "PROCESSED", "GOOD", null, "PRODUCTION");
                    log.info("Process order output added to stock: po={}, product={}, qty={}",
                            po.getProcessNumber(), po.getOutputProductId(), po.getOutputQuantity());
                } catch (Exception e) {
                    log.warn("Process order output stock addition failed for {}: {}", po.getOutputProductId(), e.getMessage());
                }
            }
        }

        return processOrderRepository.save(po);
    }

    // --- Quality Checks ---

    public PageResponse<QualityCheck> listQualityChecks(String result, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = result != null
                ? qualityCheckRepository.findByTenantIdAndResultAndDeletedAtIsNull(tenantId, result, pageable)
                : qualityCheckRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page);
    }

    @Transactional
    public QualityCheck createQualityCheck(QualityCheck qc) {
        UUID tenantId = tenantContext.current();
        qc.setTenantId(tenantId);
        if (qc.getCheckNumber() == null) {
            qc.setCheckNumber("QC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        QualityCheck saved = qualityCheckRepository.save(qc);
        log.info("QualityCheck created: id={}, number={}", saved.getId(), saved.getCheckNumber());
        return saved;
    }

    @Transactional
    public QualityCheck updateQualityCheck(UUID id, QualityCheck updated) {
        QualityCheck existing = qualityCheckRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Quality check not found: " + id));
        existing.setCheckType(updated.getCheckType());
        existing.setCheckDate(updated.getCheckDate());
        existing.setCheckedBy(updated.getCheckedBy());
        existing.setResult(updated.getResult());
        existing.setParameters(updated.getParameters());
        existing.setDefectsFound(updated.getDefectsFound());
        existing.setRemarks(updated.getRemarks());
        existing.setCorrectiveAction(updated.getCorrectiveAction());
        existing.setWorkOrderId(updated.getWorkOrderId());
        return qualityCheckRepository.save(existing);
    }

    // --- Production Jobs ---

    public PageResponse<ProductionJob> listProductionJobs(
            JobStatus status, String processType,
            String jobNumber, String locationName, String workCenter,
            String storageLocation, String lotNumber,
            java.time.LocalDate startFrom, java.time.LocalDate startTo,
            java.time.LocalDate endFrom, java.time.LocalDate endTo,
            Pageable pageable) {
        UUID tenantId = tenantContext.current();
        boolean hasExtra = jobNumber != null || locationName != null || workCenter != null
                || storageLocation != null || lotNumber != null
                || startFrom != null || startTo != null || endFrom != null || endTo != null;
        if (hasExtra || (status != null && processType != null)) {
            return PageResponse.of(productionJobRepository.findAll(
                    ProductionJobRepository.search(
                            tenantId, status, processType, jobNumber, locationName, workCenter,
                            storageLocation, lotNumber, startFrom, startTo, endFrom, endTo),
                    pageable));
        }
        List<String> locs = userContext.getAllowedLocations();
        if (userContext.hasLocationRestriction()) {
            if (status != null) return PageResponse.of(productionJobRepository.findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(tenantId, status, locs, pageable));
            if (processType != null) return PageResponse.of(productionJobRepository.findByTenantIdAndProcessTypeAndLocationsAndDeletedAtIsNull(tenantId, processType, locs, pageable));
            return PageResponse.of(productionJobRepository.findByTenantIdAndLocationsAndDeletedAtIsNull(tenantId, locs, pageable));
        }
        if (status != null) return PageResponse.of(productionJobRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        if (processType != null) return PageResponse.of(productionJobRepository.findByTenantIdAndProcessTypeAndDeletedAtIsNull(tenantId, processType, pageable));
        return PageResponse.of(productionJobRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public ProductionJob getProductionJobById(UUID id) {
        return productionJobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production job not found: " + id));
    }

    @Transactional
    public ProductionJob createProductionJob(ProductionJob job) {
        UUID tenantId = tenantContext.current();
        job.setTenantId(tenantId);
        if (job.getJobNumber() == null) {
            job.setJobNumber("PJ-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        if (job.getStatus() == null) job.setStatus(JobStatus.DRAFT);
        if ((job.getInputQuantity() == null || job.getInputQuantity().compareTo(BigDecimal.ZERO) == 0)
                && job.getInputLots() != null && !job.getInputLots().isEmpty()) {
            BigDecimal sumInput = job.getInputLots().stream()
                    .map(l -> l.getToProcess() != null ? l.getToProcess() : (l.getAvailableQty() != null ? l.getAvailableQty() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sumInput.compareTo(BigDecimal.ZERO) > 0) {
                job.setInputQuantity(sumInput);
            }
        }
        for (ProcessJobInputLot lot : job.getInputLots()) { lot.setTenantId(tenantId); lot.setJob(job); }
        for (ProcessJobOutputLine line : job.getOutputLines()) { line.setTenantId(tenantId); line.setJob(job); }
        ProductionJob saved = productionJobRepository.save(job);
        log.info("ProductionJob created: id={}, number={}", saved.getId(), saved.getJobNumber());
        return saved;
    }

    @Transactional
    public ProductionJob updateProductionJob(UUID id, ProductionJob updated) {
        ProductionJob existing = productionJobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production job not found: " + id));
        existing.setJobName(updated.getJobName());
        existing.setProcessType(updated.getProcessType());
        existing.setWorkOrderId(updated.getWorkOrderId());
        existing.setInputProductId(updated.getInputProductId());
        existing.setInputProductName(updated.getInputProductName());
        BigDecimal reqInput = updated.getInputQuantity();
        if ((reqInput == null || reqInput.compareTo(BigDecimal.ZERO) == 0) && updated.getInputLots() != null) {
            BigDecimal sumInput = updated.getInputLots().stream()
                    .map(l -> l.getToProcess() != null ? l.getToProcess() : (l.getAvailableQty() != null ? l.getAvailableQty() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sumInput.compareTo(BigDecimal.ZERO) > 0) reqInput = sumInput;
        }
        existing.setInputQuantity(reqInput != null ? reqInput : BigDecimal.ZERO);
        existing.setInputUnit(updated.getInputUnit());
        existing.setOutputProductId(updated.getOutputProductId());
        existing.setOutputProductName(updated.getOutputProductName());
        existing.setPlannedOutputQuantity(updated.getPlannedOutputQuantity());
        existing.setActualOutputQuantity(updated.getActualOutputQuantity());
        existing.setOutputUnit(updated.getOutputUnit());
        existing.setPlannedStartDate(updated.getPlannedStartDate());
        existing.setPlannedEndDate(updated.getPlannedEndDate());
        existing.setActualStartDate(updated.getActualStartDate());
        existing.setActualEndDate(updated.getActualEndDate());
        existing.setWorkCenter(updated.getWorkCenter());
        existing.setSupervisor(updated.getSupervisor());
        existing.setLaborCount(updated.getLaborCount());
        existing.setExpenditure(updated.getExpenditure());
        existing.setExpectedYield(updated.getExpectedYield());
        existing.setActualYield(updated.getActualYield());
        existing.setPackagingUnitSize(updated.getPackagingUnitSize());
        existing.setPackagingUnitSizeUnit(updated.getPackagingUnitSizeUnit());
        existing.setPackagingDate(updated.getPackagingDate());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setBatchNumber(updated.getBatchNumber());
        existing.setNotes(updated.getNotes());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setProcessStepId(updated.getProcessStepId());
        existing.setProcessStepName(updated.getProcessStepName());
        existing.setProcessLineId(updated.getProcessLineId());
        existing.setProcessLineName(updated.getProcessLineName());
        existing.setLocationName(updated.getLocationName());
        existing.setStorageLocationName(updated.getStorageLocationName());
        existing.setTimeTakenMinutes(updated.getTimeTakenMinutes());
        // Seed context + Process Job header
        existing.setCropGroupName(updated.getCropGroupName());
        existing.setCropName(updated.getCropName());
        existing.setVarietyName(updated.getVarietyName());
        existing.setSeedState(updated.getSeedState());
        existing.setInputState(updated.getInputState());
        existing.setOutputState(updated.getOutputState());
        existing.setStepsJson(updated.getStepsJson());
        existing.setGodownName(updated.getGodownName());
        existing.setGrowerName(updated.getGrowerName());
        existing.setVillageName(updated.getVillageName());
        existing.setMoisture(updated.getMoisture());
        existing.setInputType(updated.getInputType());
        existing.setSeedType(updated.getSeedType());
        existing.setStage(updated.getStage());
        existing.setMaleLabourCount(updated.getMaleLabourCount());
        existing.setFemaleLabourCount(updated.getFemaleLabourCount());
        existing.setHamaliContractor(updated.getHamaliContractor());
        existing.setOnlyQualityTested(updated.isOnlyQualityTested());

        // Sync input lots
        existing.getInputLots().clear();
        for (ProcessJobInputLot lot : updated.getInputLots()) {
            lot.setTenantId(existing.getTenantId());
            lot.setJob(existing);
            existing.getInputLots().add(lot);
        }
        // Sync output lines
        existing.getOutputLines().clear();
        for (ProcessJobOutputLine line : updated.getOutputLines()) {
            line.setTenantId(existing.getTenantId());
            line.setJob(existing);
            existing.getOutputLines().add(line);
        }
        return productionJobRepository.save(existing);
    }

    @Transactional
    public ProductionJob updateProductionJobStatus(UUID id, JobStatus status) {
        UUID tenantId = tenantContext.current();
        ProductionJob job = productionJobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Production job not found: " + id));
        JobStatus prevStatus = job.getStatus();
        job.setStatus(status);
        // A state-based seed process job is identified by having input LOTS (ProcessJobInputLot).
        // Such a job may ALSO carry an output product, so we must NOT key behaviour off
        // outputProductId — the input-lot presence is the reliable discriminator.
        boolean seedJob = job.getInputLots() != null && !job.getInputLots().isEmpty();
        if (status == JobStatus.IN_PROGRESS && job.getActualStartDate() == null) {
            job.setActualStartDate(LocalDate.now());
            // Legacy product-based jobs materialise an output LotStock at start. Seed/state-based
            // jobs must NOT (their output lot is created at completion as a StockLot with Issue/
            // Receipt), and LotStock.product_id is NOT NULL so it would also fail here.
            if (job.getLotId() == null && job.getOutputProductId() != null && !seedJob) {
                createLotForJob(tenantId, job);
            }
        }
        if (status == JobStatus.COMPLETED && prevStatus != JobStatus.COMPLETED) {
            // Block completion if any quality checks on the linked work order have FAIL results
            if (job.getWorkOrderId() != null
                    && qualityCheckRepository.existsByTenantIdAndWorkOrderIdAndResultAndDeletedAtIsNull(
                            tenantId, job.getWorkOrderId(), "FAIL")) {
                throw AppException.badRequest(
                        "Cannot complete production job: quality checks on linked work order have FAIL results. Resolve quality issues first.");
            }
            if (job.getActualEndDate() == null) job.setActualEndDate(LocalDate.now());
            if (job.getActualStartDate() != null && job.getTimeTakenMinutes() == null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(job.getActualStartDate(), job.getActualEndDate());
                job.setTimeTakenMinutes((int) Math.max(0, days) * 480); // 8 working hrs/day
            }

            if ((job.getInputQuantity() == null || job.getInputQuantity().compareTo(BigDecimal.ZERO) == 0)
                    && job.getInputLots() != null && !job.getInputLots().isEmpty()) {
                BigDecimal sumInput = job.getInputLots().stream()
                        .map(l -> l.getToProcess() != null ? l.getToProcess() : (l.getAvailableQty() != null ? l.getAvailableQty() : BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (sumInput.compareTo(BigDecimal.ZERO) > 0) {
                    job.setInputQuantity(sumInput);
                }
            }

            log.info("Process job {} completing: seedJob={}, inputLots={}, inputProductId={}, outputProductId={}",
                    job.getJobNumber(), seedJob,
                    (job.getInputLots() == null ? 0 : job.getInputLots().size()),
                    job.getInputProductId(), job.getOutputProductId());

            if (seedJob) {
                // Lot-based (e.g. Fresh -> Grading): ISSUE each input lot (reduce its StockLot +
                // InventoryIssue), RECEIVE the graded output (new StockLot + InventoryReceipt), and
                // write a SeedConversion audit.
                try {
                    postSeedProcessJobInventory(tenantId, job);
                } catch (Exception e) {
                    log.error("Seed process-job inventory posting failed for job {}", job.getJobNumber(), e);
                }
            } else {
                // Product/quantity-based job (no input lots): deduct aggregate stock, and STILL record
                // an Issue (input) and a Receipt (output) so every completed job has its transactions.
                if (job.getInputProductId() != null && job.getInputQuantity() != null
                        && job.getInputQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        stockService.deductStock(job.getInputProductId(), null, job.getInputQuantity(),
                                "PRODUCTION_JOB", job.getId(), job.getJobNumber(),
                                "RAW", "GOOD", null, "PRODUCTION_CONSUME");
                    } catch (Exception e) {
                        log.warn("Input stock deduction skipped for job {}: {}", job.getJobNumber(), e.getMessage());
                    }
                }
                try {
                    recordJobIssueReceipt(tenantId, job);
                } catch (Exception e) {
                    log.error("Issue/Receipt recording failed for job {}", job.getJobNumber(), e);
                }
            }

            // Inventory posting is best-effort above; if it failed, the job would still be marked
            // complete with no output recorded. Derive it here so a finished job never reports zero.
            if (job.getActualOutputQuantity() == null
                    || job.getActualOutputQuantity().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal derived = firstPositive(sumOutputLineQuantity(job),
                        job.getPlannedOutputQuantity(), job.getInputQuantity());
                if (derived != null && derived.compareTo(BigDecimal.ZERO) > 0) {
                    job.setActualOutputQuantity(derived);
                }
            }
        }
        return productionJobRepository.save(job);
    }

    /**
     * Issue (input) + Receipt (output) transactions for a product/quantity-based process job that
     * has no input lots. Uses the job's product/state + quantity fields (detail lives in notes/
     * packDetails since these are header-only entities).
     */
    private void recordJobIssueReceipt(UUID tenantId, ProductionJob job) {
        InventoryIssue iss = new InventoryIssue();
        iss.setTenantId(tenantId);
        iss.setIssueNumber(String.format("ISS-%05d", inventoryIssueRepository.countByTenantId(tenantId) + 1));
        iss.setGodownName(job.getGodownName());
        iss.setLocation(job.getLocationName());
        iss.setIssueDate(LocalDate.now());
        iss.setIssueTo("Process Job " + nz(job.getJobNumber()));
        iss.setQuantity(job.getInputQuantity());
        iss.setUnit(job.getInputUnit());
        String inName = job.getInputProductName() != null ? job.getInputProductName() : nz(job.getInputState());
        iss.setNotes("Process Job " + nz(job.getJobNumber()) + " input consumption — " + inName
                + (job.getInputQuantity() != null ? ", qty " + job.getInputQuantity().toPlainString() : ""));
        inventoryIssueRepository.save(iss);

        InventoryReceipt rcp = new InventoryReceipt();
        rcp.setTenantId(tenantId);
        rcp.setReceiptNumber(String.format("RCP-%05d", inventoryReceiptRepository.countByTenantId(tenantId) + 1));
        rcp.setGodownName(job.getGodownName());
        rcp.setLocation(job.getLocationName());
        rcp.setReceiptDate(LocalDate.now());
        String outName = job.getOutputProductName() != null ? job.getOutputProductName() : nz(job.getOutputState());
        // Yield-driven jobs such as grading carry no planned output — the quantity is only known
        // once the job runs. Falling back to what was consumed (as the lot-based path already
        // does) keeps the receipt from being recorded as zero against a job that clearly produced
        // something. An operator who measured a real yield sets actualOutputQuantity and wins.
        BigDecimal outQty = firstPositive(job.getActualOutputQuantity(), sumOutputLineQuantity(job),
                job.getPlannedOutputQuantity(), job.getInputQuantity());
        rcp.setQuantity(outQty);
        rcp.setUnit(job.getOutputUnit());
        rcp.setLotNumber(job.getLotNumber());
        rcp.setPackDetails("Process Job " + nz(job.getJobNumber()) + " output — " + outName
                + (outQty != null ? ", qty " + outQty.toPlainString() : "")
                + (job.getLotNumber() != null ? ", lot " + job.getLotNumber() : ""));
        inventoryReceiptRepository.save(rcp);
        // Record what was actually produced on the job itself, so the job detail reports its
        // output instead of falling back to a planned figure that was never entered.
        if (job.getActualOutputQuantity() == null || job.getActualOutputQuantity().compareTo(BigDecimal.ZERO) == 0) {
            job.setActualOutputQuantity(outQty);
        }
        log.info("Process job {} issue/receipt recorded (non-lot path): {} / {}, output qty {}",
                job.getJobNumber(), iss.getIssueNumber(), rcp.getReceiptNumber(), outQty);
    }

    /**
     * Physical-inventory posting for a state-based seed process job on completion:
     *  - ISSUE: reduce each input lot's StockLot quantity/bags by the amount processed.
     *  - RECEIVE: create one graded output StockLot (new auto lot number, output seed state, same
     *    variety/crop/godown) so physical inventory reflects the graded stock and it is traceable.
     *  - AUDIT: write a SeedConversion record capturing from-lot/qty/bags -> to-qty/bags.
     * Only runs for state-based jobs (no output product); product-based jobs keep the LotStock path.
     */
    private void postSeedProcessJobInventory(UUID tenantId, ProductionJob job) {
        // Caller gates on seedJob (has input lots) and on the transition INTO completed, so this
        // runs exactly once per job.
        StockLot sourceLot = null;
        BigDecimal totalIssued = BigDecimal.ZERO;
        int issuedBags = 0;
        long issSeq = inventoryIssueRepository.countByTenantId(tenantId);

        if (job.getInputLots() != null) {
            for (ProcessJobInputLot in : job.getInputLots()) {
                if (in.getLotNumber() == null || in.getLotNumber().isBlank()) continue;
                BigDecimal want = (in.getToProcess() != null && in.getToProcess().compareTo(BigDecimal.ZERO) > 0)
                        ? in.getToProcess()
                        : (in.getAvailableQty() != null ? in.getAvailableQty() : BigDecimal.ZERO);
                BigDecimal lotIssued = BigDecimal.ZERO;
                int lotBags = 0;
                StockLot ctx = null;
                for (StockLot sl : stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, in.getLotNumber().trim())) {
                    if (sourceLot == null) sourceLot = sl;
                    if (ctx == null) ctx = sl;
                    BigDecimal have = sl.getQuantity() == null ? BigDecimal.ZERO : sl.getQuantity();
                    BigDecimal take = want.min(have);
                    if (have.compareTo(BigDecimal.ZERO) > 0 && take.compareTo(BigDecimal.ZERO) > 0) {
                        int bags = sl.getNoOfBags() == null ? 0 : sl.getNoOfBags();
                        int bagsOut = (int) Math.round(bags * (take.doubleValue() / have.doubleValue()));
                        sl.setQuantity(have.subtract(take));
                        sl.setNoOfBags(Math.max(0, bags - bagsOut));
                        stockLotRepository.save(sl);
                        lotIssued = lotIssued.add(take);
                        lotBags += bagsOut;
                        want = want.subtract(take);
                    }
                    if (want.compareTo(BigDecimal.ZERO) <= 0) break;
                }
                totalIssued = totalIssued.add(lotIssued);
                issuedBags += lotBags;

                // ISSUE transaction for this input lot
                InventoryIssue iss = new InventoryIssue();
                iss.setTenantId(tenantId);
                iss.setIssueNumber(String.format("ISS-%05d", ++issSeq));
                iss.setLocation(ctx != null ? ctx.getLocation() : job.getLocationName());
                iss.setGodownId(ctx != null ? ctx.getGodownId() : null);
                iss.setGodownName(ctx != null ? ctx.getGodownName() : job.getGodownName());
                iss.setIssueDate(LocalDate.now());
                iss.setIssueTo("Process Job " + nz(job.getJobNumber()));
                iss.setQuantity(lotIssued);
                iss.setUnit(job.getInputUnit() != null ? job.getInputUnit() : (ctx != null ? ctx.getUnit() : null));
                iss.setLotNumber(in.getLotNumber().trim());
                iss.setNotes("Process Job " + nz(job.getJobNumber()) + " input consumption — lot "
                        + in.getLotNumber().trim() + ", qty " + lotIssued.toPlainString() + ", bags " + lotBags
                        + (job.getInputState() != null ? " (" + job.getInputState() + ")" : ""));
                inventoryIssueRepository.save(iss);

                // Lot-keyed issue detail so this shows under "Issues" in Lot Traceability for the input lot.
                LotIssueDetail lid = new LotIssueDetail();
                lid.setTenantId(tenantId);
                lid.setIssueNumber(iss.getIssueNumber());
                lid.setIssueDate(LocalDate.now());
                lid.setLotNumber(in.getLotNumber().trim());
                lid.setLocation(firstNonBlankStr(ctx != null ? ctx.getLocation() : null, job.getLocationName(),
                        resolveGodownLocation(tenantId, ctx != null ? ctx.getGodownId() : null, ctx != null ? ctx.getGodownName() : null)));
                lid.setGodown(ctx != null ? ctx.getGodownName() : job.getGodownName());
                lid.setQuantity(lotIssued);
                lid.setUnit(job.getInputUnit() != null ? job.getInputUnit() : (ctx != null ? ctx.getUnit() : null));
                lid.setIssueType("PROCESSING");
                lid.setRemarks("Process Job " + nz(job.getJobNumber()));
                lotIssueDetailRepository.save(lid);
            }
        }

        // The packed/graded output lines are the most truthful figure — they are what the operator
        // recorded coming out — so they outrank a planned number and the consumed fallback.
        BigDecimal outQty = firstPositive(job.getActualOutputQuantity(), sumOutputLineQuantity(job),
                job.getPlannedOutputQuantity(), totalIssued);
        int outBags = sumOutputLineBags(job);
        if (outBags == 0) outBags = issuedBags;

        String outLotNo = generateStockLotNo(tenantId);
        String outState = (job.getOutputState() != null && !job.getOutputState().isBlank())
                ? job.getOutputState() : mapProcessTypeToMaterialState(job.getProcessType());

        StockLot out = new StockLot();
        out.setTenantId(tenantId);
        out.setLotNo(outLotNo);
        out.setCropGroupName(sourceLot != null ? sourceLot.getCropGroupName() : job.getCropGroupName());
        out.setCropName(sourceLot != null ? sourceLot.getCropName() : job.getCropName());
        out.setVarietyName(sourceLot != null ? sourceLot.getVarietyName() : job.getVarietyName());
        // The graded/packed output lot lives in the OUTPUT line's godown (where the operator
        // placed it) — NOT the input lot's godown. Fall back to input lot / job godown.
        String outputLineGodown = firstOutputLineGodown(job);
        String outGodownName = firstNonBlankStr(outputLineGodown,
                sourceLot != null ? sourceLot.getGodownName() : null, job.getGodownName());
        UUID outGodownId = resolveGodownIdByName(tenantId, outGodownName);
        if (outGodownId == null && outputLineGodown == null && sourceLot != null) outGodownId = sourceLot.getGodownId();
        out.setGodownId(outGodownId);
        out.setGodownName(outGodownName);
        // Location must be captured on the graded lot. Prefer the input lot's location, then the
        // job's, then resolve it from the godown (godown.location is the plant/location name).
        String outLocation = firstNonBlankStr(
                // If the operator picked an output godown, use ITS plant/location first.
                outputLineGodown != null ? resolveGodownLocation(tenantId, outGodownId, outGodownName) : null,
                sourceLot != null ? sourceLot.getLocation() : null,
                job.getLocationName(),
                resolveGodownLocation(tenantId, outGodownId, outGodownName));
        out.setLocation(outLocation);
        out.setMaterialType(sourceLot != null ? sourceLot.getMaterialType() : null);
        out.setMaterialState(outState);
        out.setNoOfBags(outBags);
        out.setQuantity(outQty);
        out.setOriginalNoOfBags(outBags);
        out.setOriginalQuantity(outQty);
        out.setUnit(job.getOutputUnit() != null ? job.getOutputUnit() : (sourceLot != null ? sourceLot.getUnit() : "KG"));
        out.setSource("PROCESS_JOB");
        // Carry the cost forward so the graded output is valued rather than appearing free. The
        // input lot's cost per unit is the closest thing to a true cost for what came out of it;
        // conversion gain or loss is not apportioned here.
        if (sourceLot != null && sourceLot.getUnitCost() != null && sourceLot.getUnitCost().signum() > 0) {
            out.setUnitCost(sourceLot.getUnitCost());
        }
        stockLotRepository.save(out);

        // RECEIPT transaction for the graded output lot
        InventoryReceipt rcp = new InventoryReceipt();
        rcp.setTenantId(tenantId);
        rcp.setReceiptNumber(String.format("RCP-%05d", inventoryReceiptRepository.countByTenantId(tenantId) + 1));
        rcp.setLocation(out.getLocation());
        rcp.setGodownId(out.getGodownId());
        rcp.setGodownName(out.getGodownName());
        rcp.setReceiptDate(LocalDate.now());
        rcp.setQuantity(outQty);
        rcp.setUnit(out.getUnit());
        rcp.setLotNumber(outLotNo);
        rcp.setPackDetails("Process Job " + nz(job.getJobNumber()) + " graded output — lot " + outLotNo
                + ", qty " + (outQty != null ? outQty.toPlainString() : "0") + ", bags " + outBags
                + " (" + outState + ")");
        inventoryReceiptRepository.save(rcp);

        SeedConversion conv = new SeedConversion();
        conv.setTenantId(tenantId);
        conv.setCropGroupName(out.getCropGroupName());
        conv.setCropName(out.getCropName());
        conv.setVarietyName(out.getVarietyName());
        conv.setGodownId(out.getGodownId());
        conv.setLocation(out.getLocation());
        conv.setConversionDate(LocalDate.now());
        conv.setFromLotNo(firstInputLotNo(job));
        conv.setFromQuantity(totalIssued.toPlainString());
        conv.setFromNoOfBags(String.valueOf(issuedBags));
        conv.setToQuantity(outQty != null ? outQty.toPlainString() : "0");
        conv.setToNoOfBags(String.valueOf(outBags));
        conv.setNotes("Auto: Process Job " + job.getJobNumber() + " (" + nz(job.getInputState()) + " -> " + outState
                + "), output lot " + outLotNo);
        // Marked at the source rather than inferred later from the wording of the note.
        conv.setSource("PROCESS_JOB");
        conv.setSourceJobId(job.getId());
        conv.setSourceReference(job.getJobNumber());
        seedConversionRepository.save(conv);

        job.setLotNumber(outLotNo);
        if (job.getActualOutputQuantity() == null || job.getActualOutputQuantity().compareTo(BigDecimal.ZERO) == 0)
            job.setActualOutputQuantity(outQty);
        log.info("Process job {} completed: issued {} ({} bags) from inputs, received {} ({} bags) as graded lot {} [{}]",
                job.getJobNumber(), totalIssued, issuedBags, outQty, outBags, outLotNo, outState);
    }

    private String generateStockLotNo(UUID tenantId) {
        long existing = stockLotRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("LOT-%d-%04d", LocalDate.now().getYear(), existing + 1);
    }

    private static BigDecimal firstPositive(BigDecimal... vals) {
        BigDecimal lastNonNull = BigDecimal.ZERO;
        for (BigDecimal v : vals) {
            if (v == null) continue;
            lastNonNull = v;
            if (v.compareTo(BigDecimal.ZERO) > 0) return v;
        }
        return lastNonNull;
    }

    /** The godown chosen on the output line — where the graded/packed output lot is placed. */
    private static String firstOutputLineGodown(ProductionJob job) {
        if (job.getOutputLines() == null) return null;
        for (ProcessJobOutputLine l : job.getOutputLines()) {
            if (l.getStorageLocationName() != null && !l.getStorageLocationName().isBlank())
                return l.getStorageLocationName().trim();
        }
        return null;
    }

    private UUID resolveGodownIdByName(UUID tenantId, String name) {
        if (name == null || name.isBlank()) return null;
        return godownRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name.trim())
                .map(g -> g.getId()).orElse(null);
    }

    private static int sumOutputLineBags(ProductionJob job) {
        if (job.getOutputLines() == null) return 0;
        int sum = 0;
        for (ProcessJobOutputLine l : job.getOutputLines()) {
            if (l.getNumberOfUnits() != null) sum += l.getNumberOfUnits();
        }
        return sum;
    }

    /**
     * What the output lines actually add up to: units × pack size, summed.
     *
     * <p>A packing job states its output as lines — "40 packets of 25 kg" — and never as a single
     * quantity on the job. Nothing derived a total from them, so a completed packing job reported
     * zero output next to a perfectly good input quantity. Returns zero when the lines carry no
     * pack size, in which case the caller falls back to what was consumed.
     */
    private static BigDecimal sumOutputLineQuantity(ProductionJob job) {
        if (job.getOutputLines() == null) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (ProcessJobOutputLine l : job.getOutputLines()) {
            if (l.getNumberOfUnits() == null || l.getPackSize() == null) continue;
            // Convert before multiplying. A 500 g pack times ten packets is 5 kg, not 5,000 —
            // multiplying first and labelling the result kilograms is what produced output a
            // thousand times the input it came from.
            BigDecimal packKg = toKilograms(l.getPackSize(), l.getPackSizeUom());
            total = total.add(packKg.multiply(BigDecimal.valueOf(l.getNumberOfUnits())));
        }
        return total;
    }

    /**
     * A pack size in whatever unit it was captured, expressed in kilograms.
     *
     * <p>Kilograms is the base every input quantity, lot balance and yield percentage in this
     * module is already stated in, so output has to reach the same footing before it can be
     * compared with them. An unrecognised or absent unit is taken as kilograms — that is what
     * every line written before pack sizes carried a unit meant.
     */
    static BigDecimal toKilograms(BigDecimal value, String uom) {
        if (value == null) return BigDecimal.ZERO;
        if (uom == null || uom.isBlank()) return value;
        String u = uom.trim().toUpperCase().replace(".", "");
        return switch (u) {
            case "G", "GM", "GMS", "GRAM", "GRAMS"            -> value.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            case "MG", "MILLIGRAM", "MILLIGRAMS"              -> value.divide(BigDecimal.valueOf(1_000_000), 9, RoundingMode.HALF_UP);
            case "QTL", "QUINTAL", "QUINTALS"                 -> value.multiply(BigDecimal.valueOf(100));
            case "T", "TON", "TONNE", "TONS", "TONNES", "MT"  -> value.multiply(BigDecimal.valueOf(1000));
            default                                            -> value;   // KG, KGS, KILOGRAM, or unknown
        };
    }

    private static String firstInputLotNo(ProductionJob job) {
        if (job.getInputLots() == null) return null;
        for (ProcessJobInputLot in : job.getInputLots()) {
            if (in.getLotNumber() != null && !in.getLotNumber().isBlank()) return in.getLotNumber().trim();
        }
        return null;
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String firstNonBlankStr(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    /** Resolve a godown's plant/location name (godown.location) — used to backfill a lot's Location. */
    private String resolveGodownLocation(UUID tenantId, UUID godownId, String godownName) {
        if (godownId == null) return null;
        return godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, godownId)
                .map(g -> g.getLocation()).filter(l -> l != null && !l.isBlank()).orElse(null);
    }

    private String mapProcessTypeToMaterialState(String processType) {
        if (processType == null) return "PROCESSED";
        return switch (processType) {
            case "GRADING" -> "GRADED";
            case "PACKAGING" -> "PACKED";
            case "TREATMENT" -> "TREATED";
            default -> "PROCESSED";
        };
    }

    private void createLotForJob(UUID tenantId, ProductionJob job) {
        StorageLocation location = storageLocationRepository
                .findFirstByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(tenantId)
                .orElseThrow(() -> AppException.badRequest(
                        "No default storage location configured. Please mark one storage location as default."));

        String lotNumber = "LOT-" + job.getJobNumber();

        LotStock lot = new LotStock();
        lot.setTenantId(tenantId);
        lot.setLotNumber(lotNumber);
        lot.setProductId(job.getOutputProductId());
        lot.setProductName(job.getOutputProductName());
        lot.setWarehouseId(location.getWarehouseId());
        lot.setWarehouseName(location.getWarehouseName());
        lot.setStorageLocationId(location.getId());
        lot.setStorageLocationName(location.getName());
        lot.setSourceType(LotSource.PRODUCTION);
        lot.setSourceId(job.getId());
        lot.setProductionDate(LocalDate.now());
        lot.setExpiryDate(job.getExpiryDate());
        lot.setQuantityOnHand(job.getPlannedOutputQuantity());
        lot.setStatus(LotStockStatus.AVAILABLE);
        LotStock savedLot = lotStockRepository.save(lot);

        LotMovement movement = new LotMovement();
        movement.setTenantId(tenantId);
        movement.setLotNumber(lotNumber);
        movement.setProductId(job.getOutputProductId());
        movement.setProductName(job.getOutputProductName());
        movement.setWarehouseId(location.getWarehouseId());
        movement.setMovementType(LotMovType.RECEIPT);
        movement.setMovementDate(LocalDate.now());
        movement.setQuantity(job.getPlannedOutputQuantity());
        movement.setBalanceBefore(java.math.BigDecimal.ZERO);
        movement.setBalanceAfter(job.getPlannedOutputQuantity());
        movement.setReferenceType("PRODUCTION_JOB");
        movement.setReferenceId(job.getId());
        movement.setReferenceNumber(job.getJobNumber());
        lotMovementRepository.save(movement);

        job.setLotId(savedLot.getId());
        job.setLotNumber(lotNumber);
        job.setStorageLocationId(location.getId());
        job.setStorageLocationName(location.getName());

        // Mirror quantity into the aggregate stock_items table so Inventory Stock reflects the addition
        try {
            stockService.addStock(job.getOutputProductId(), location.getWarehouseId(),
                    job.getPlannedOutputQuantity(), "PRODUCTION_JOB", job.getId(), job.getJobNumber(),
                    job.getOutputProductName(), mapProcessTypeToMaterialState(job.getProcessType()), "GOOD",
                    lotNumber, "PRODUCTION");
        } catch (Exception e) {
            log.warn("Stock item update failed for production job {}: {}", job.getJobNumber(), e.getMessage());
        }

        log.info("Lot created on job initiation: lotNumber={}, jobNumber={}, location={}",
                lotNumber, job.getJobNumber(), location.getName());
    }

    @Transactional
    public void deleteProductionJob(UUID id) {
        ProductionJob job = productionJobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production job not found: " + id));
        job.setDeletedAt(java.time.LocalDateTime.now());
        productionJobRepository.save(job);
        log.info("ProductionJob soft-deleted: id={}", id);
    }
}
