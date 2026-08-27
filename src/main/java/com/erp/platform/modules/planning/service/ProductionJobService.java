package com.erp.platform.modules.planning.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.planning.dto.CreateProductionJobRequest;
import com.erp.platform.modules.planning.dto.ProductionJobDto;
import com.erp.platform.modules.planning.entity.ProductionJob;
import com.erp.platform.modules.planning.entity.ProductionJob.JobStatus;
import com.erp.platform.modules.planning.entity.ProductionPlan;
import com.erp.platform.modules.planning.repository.PlanningJobRepository;
import com.erp.platform.modules.planning.repository.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductionJobService {

    private final PlanningJobRepository productionJobRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final StockService stockService;
    private final LotStockRepository lotStockRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionJobDto> list(UUID planId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (planId != null) {
            ProductionPlan plan = productionPlanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, planId)
                    .orElseThrow(() -> AppException.notFound("Production plan not found: " + planId));
            return PageResponse.of(
                    productionJobRepository.findByTenantIdAndPlanAndDeletedAtIsNull(tenantId, plan, pageable)
                            .map(this::toDto));
        }
        return PageResponse.of(
                productionJobRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProductionJobDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionJobDto create(CreateProductionJobRequest request) {
        UUID tenantId = tenantContext.current();

        ProductionPlan plan = productionPlanRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getPlanId())
                .orElseThrow(() -> AppException.notFound("Production plan not found: " + request.getPlanId()));

        ProductionJob job = new ProductionJob();
        job.setTenantId(tenantId);
        job.setJobNumber(generateJobNumber(tenantId));
        job.setPlan(plan);
        job.setProductId(request.getProductId());
        job.setProductName(request.getProductName());
        job.setWorkOrderId(request.getWorkOrderId());
        job.setJobType(request.getJobType() != null ? request.getJobType() : ProductionJob.JobType.PRODUCTION);
        job.setStatus(JobStatus.OPEN);
        job.setPlannedQuantity(request.getPlannedQuantity() != null ? request.getPlannedQuantity() : BigDecimal.ZERO);
        job.setCompletedQuantity(BigDecimal.ZERO);
        job.setScheduledDate(request.getScheduledDate());
        job.setAssignedTeam(request.getAssignedTeam());
        job.setProductionCentre(request.getProductionCentre());
        job.setNotes(request.getNotes());
        job.setCropGroupId(request.getCropGroupId());
        job.setCropGroupName(request.getCropGroupName());
        job.setCropDataId(request.getCropDataId());
        job.setCropName(request.getCropName());
        job.setPlantVariantId(request.getPlantVariantId());
        job.setVarietyName(request.getVarietyName());
        job.setProductionAreaId(request.getProductionAreaId());
        job.setProductionAreaName(request.getProductionAreaName());

        job = productionJobRepository.save(job);
        log.info("ProductionJob created: id={}, number={}", job.getId(), job.getJobNumber());
        return toDto(job);
    }

    @Transactional
    public ProductionJobDto update(UUID id, CreateProductionJobRequest request) {
        ProductionJob job = findOrThrow(id);
        if (job.getStatus() != JobStatus.OPEN) {
            throw AppException.badRequest("Only OPEN production jobs can be edited");
        }
        if (request.getProductId() != null) job.setProductId(request.getProductId());
        if (request.getProductName() != null) job.setProductName(request.getProductName());
        job.setWorkOrderId(request.getWorkOrderId());
        if (request.getJobType() != null) job.setJobType(request.getJobType());
        if (request.getPlannedQuantity() != null) job.setPlannedQuantity(request.getPlannedQuantity());
        job.setScheduledDate(request.getScheduledDate());
        job.setAssignedTeam(request.getAssignedTeam());
        job.setProductionCentre(request.getProductionCentre());
        job.setNotes(request.getNotes());
        if (request.getCropGroupId() != null) job.setCropGroupId(request.getCropGroupId());
        if (request.getCropGroupName() != null) job.setCropGroupName(request.getCropGroupName());
        if (request.getCropDataId() != null) job.setCropDataId(request.getCropDataId());
        if (request.getCropName() != null) job.setCropName(request.getCropName());
        if (request.getPlantVariantId() != null) job.setPlantVariantId(request.getPlantVariantId());
        if (request.getVarietyName() != null) job.setVarietyName(request.getVarietyName());
        if (request.getProductionAreaId() != null) job.setProductionAreaId(request.getProductionAreaId());
        if (request.getProductionAreaName() != null) job.setProductionAreaName(request.getProductionAreaName());
        job = productionJobRepository.save(job);
        log.info("ProductionJob updated: id={}", id);
        return toDto(job);
    }

    @Transactional
    public ProductionJobDto initiate(UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionJob job = findOrThrow(id);
        if (job.getStatus() != JobStatus.OPEN) {
            throw AppException.badRequest("Only OPEN jobs can be initiated");
        }
        job.setStatus(JobStatus.IN_PROGRESS);
        job = productionJobRepository.save(job);

        // Create a production lot for tracking finished output
        if (job.getProductId() != null) {
            try {
                UUID warehouseId = stockService.getDefaultWarehouseId(tenantId);
                LotStock lot = new LotStock();
                lot.setTenantId(tenantId);
                lot.setLotNumber("LOT-" + job.getJobNumber());
                lot.setProductId(job.getProductId());
                lot.setProductName(job.getProductName());
                lot.setWarehouseId(warehouseId);
                lot.setSourceType(LotStock.LotSource.PRODUCTION);
                lot.setSourceId(job.getId());
                lot.setProductionDate(LocalDate.now());
                lot.setQuantityOnHand(BigDecimal.ZERO);
                lot.setStatus(LotStock.LotStockStatus.AVAILABLE);
                lotStockRepository.save(lot);
                log.info("Production lot created: lot=LOT-{}, job={}", job.getJobNumber(), id);
            } catch (Exception e) {
                log.warn("Could not create production lot for job {}: {}", id, e.getMessage());
            }
        }

        log.info("ProductionJob initiated: id={}", id);
        return toDto(job);
    }

    @Transactional
    public ProductionJobDto updateStatus(UUID id, JobStatus status) {
        UUID tenantId = tenantContext.current();
        ProductionJob job = findOrThrow(id);
        job.setStatus(status);
        if (status == JobStatus.COMPLETED) {
            job.setCompletedDate(LocalDate.now());
            // Add finished goods to inventory
            if (job.getProductId() != null) {
                BigDecimal completedQty = job.getCompletedQuantity() != null
                        && job.getCompletedQuantity().compareTo(BigDecimal.ZERO) > 0
                        ? job.getCompletedQuantity() : job.getPlannedQuantity();
                if (completedQty != null && completedQty.compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        stockService.addStock(job.getProductId(), null, completedQty,
                                "PRODUCTION", job.getId(), job.getJobNumber(), null,
                                "PROCESSED", "GOOD", "LOT-" + job.getJobNumber(), "PRODUCTION");
                        // Update the production lot quantity
                        final BigDecimal finalQty = completedQty;
                        lotStockRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(
                                tenantId, "LOT-" + job.getJobNumber())
                                .stream().findFirst()
                                .ifPresent(lot -> {
                                    lot.setQuantityOnHand(finalQty);
                                    lotStockRepository.save(lot);
                                });
                    } catch (Exception e) {
                        log.warn("Stock update skipped for completed job {}: {}", id, e.getMessage());
                    }
                }
            }
        }
        return toDto(productionJobRepository.save(job));
    }

    @Transactional
    public void delete(UUID id) {
        ProductionJob job = findOrThrow(id);
        if (job.getStatus() != JobStatus.OPEN) {
            throw AppException.badRequest("Only OPEN jobs can be deleted");
        }
        job.setDeletedAt(LocalDateTime.now());
        productionJobRepository.save(job);
        log.info("ProductionJob soft-deleted: id={}", id);
    }

    private ProductionJobDto toDto(ProductionJob job) {
        ProductionJobDto dto = new ProductionJobDto();
        dto.setId(job.getId());
        dto.setTenantId(job.getTenantId());
        dto.setJobNumber(job.getJobNumber());
        if (job.getPlan() != null) {
            dto.setPlanId(job.getPlan().getId());
            dto.setPlanNumber(job.getPlan().getPlanNumber());
            dto.setPlanName(job.getPlan().getName());
        }
        dto.setProductId(job.getProductId());
        dto.setProductName(job.getProductName());
        dto.setWorkOrderId(job.getWorkOrderId());
        dto.setJobType(job.getJobType());
        dto.setStatus(job.getStatus());
        dto.setPlannedQuantity(job.getPlannedQuantity());
        dto.setCompletedQuantity(job.getCompletedQuantity());
        dto.setScheduledDate(job.getScheduledDate());
        dto.setCompletedDate(job.getCompletedDate());
        dto.setAssignedTeam(job.getAssignedTeam());
        dto.setProductionCentre(job.getProductionCentre());
        dto.setNotes(job.getNotes());
        dto.setCropGroupId(job.getCropGroupId());
        dto.setCropGroupName(job.getCropGroupName());
        dto.setCropDataId(job.getCropDataId());
        dto.setCropName(job.getCropName());
        dto.setPlantVariantId(job.getPlantVariantId());
        dto.setVarietyName(job.getVarietyName());
        dto.setProductionAreaId(job.getProductionAreaId());
        dto.setProductionAreaName(job.getProductionAreaName());
        dto.setCreatedAt(job.getCreatedAt());
        dto.setUpdatedAt(job.getUpdatedAt());
        return dto;
    }

    private ProductionJob findOrThrow(UUID id) {
        return productionJobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production job not found: " + id));
    }

    private String generateJobNumber(UUID tenantId) {
        long count = productionJobRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("JOB-%s-%03d", year, count + 1);
    }
}
