package com.erp.platform.modules.planning.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.planning.dto.CreateProductionJobAllocationRequest;
import com.erp.platform.modules.planning.dto.ProductionJobAllocationDto;
import com.erp.platform.modules.planning.entity.ProductionJob;
import com.erp.platform.modules.planning.entity.ProductionJobAllocation;
import com.erp.platform.modules.planning.repository.PlanningJobRepository;
import com.erp.platform.modules.planning.repository.ProductionJobAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionJobAllocationService {

    private final ProductionJobAllocationRepository allocationRepository;
    private final PlanningJobRepository jobRepository;
    private final TenantContext tenantContext;

    public List<ProductionJobAllocationDto> listByJob(UUID jobId) {
        return allocationRepository
                .findByTenantIdAndJobIdAndDeletedAtIsNull(tenantContext.current(), jobId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ProductionJobAllocationDto create(CreateProductionJobAllocationRequest req) {
        UUID tenantId = tenantContext.current();
        ProductionJob job = jobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + req.getJobId()));

        ProductionJobAllocation alloc = new ProductionJobAllocation();
        alloc.setTenantId(tenantId);
        alloc.setJob(job);
        alloc.setAllocateeType(req.getAllocateeType());
        alloc.setSubType(req.getSubType());
        alloc.setReferenceId(req.getReferenceId());
        alloc.setReferenceName(req.getReferenceName());
        alloc.setQuantityKgs(req.getQuantityKgs() != null ? req.getQuantityKgs() : BigDecimal.ZERO);
        alloc.setAcreageAcres(req.getAcreageAcres() != null ? req.getAcreageAcres() : BigDecimal.ZERO);
        alloc.setAdvancePaid(req.getAdvancePaid() != null ? req.getAdvancePaid() : BigDecimal.ZERO);
        alloc.setExpectedYieldKgs(calculateExpectedYield(alloc.getQuantityKgs(), alloc.getAcreageAcres()));
        return toDto(allocationRepository.save(alloc));
    }

    @Transactional
    public ProductionJobAllocationDto payAdvance(UUID id, BigDecimal amount) {
        UUID tenantId = tenantContext.current();
        ProductionJobAllocation alloc = allocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        alloc.setAdvancePaid(alloc.getAdvancePaid().add(amount));
        return toDto(allocationRepository.save(alloc));
    }

    @Transactional
    public ProductionJobAllocationDto initiate(UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionJobAllocation alloc = allocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        if (!alloc.isInitiated()) {
            alloc.setTemporaryLotNumber(generateTempLotNumber(tenantId));
            alloc.setInitiated(true);
            allocationRepository.save(alloc);
        }
        return toDto(alloc);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ProductionJobAllocation alloc = allocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        alloc.setDeletedAt(LocalDateTime.now());
        allocationRepository.save(alloc);
    }

    private BigDecimal calculateExpectedYield(BigDecimal quantityKgs, BigDecimal acreageAcres) {
        if (acreageAcres == null || acreageAcres.compareTo(BigDecimal.ZERO) == 0) return quantityKgs;
        // Expected yield = quantity × 1.2 as a simple estimate (overrideable)
        return quantityKgs.multiply(new BigDecimal("1.2")).setScale(3, RoundingMode.HALF_UP);
    }

    private String generateTempLotNumber(UUID tenantId) {
        long count = allocationRepository.count();
        return String.format("TL-%s-%05d", Year.now().getValue(), count + 1);
    }

    private ProductionJobAllocationDto toDto(ProductionJobAllocation e) {
        ProductionJobAllocationDto dto = new ProductionJobAllocationDto();
        dto.setId(e.getId());
        dto.setJobId(e.getJob().getId());
        dto.setAllocateeType(e.getAllocateeType());
        dto.setSubType(e.getSubType());
        dto.setReferenceId(e.getReferenceId());
        dto.setReferenceName(e.getReferenceName());
        dto.setQuantityKgs(e.getQuantityKgs());
        dto.setAcreageAcres(e.getAcreageAcres());
        dto.setExpectedYieldKgs(e.getExpectedYieldKgs());
        dto.setAdvancePaid(e.getAdvancePaid());
        dto.setTemporaryLotNumber(e.getTemporaryLotNumber());
        dto.setInitiated(e.isInitiated());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
