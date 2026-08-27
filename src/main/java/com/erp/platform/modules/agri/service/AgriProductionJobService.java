package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.AgriJobAllocationDto;
import com.erp.platform.modules.agri.dto.CreateProductionJobRequest;
import com.erp.platform.modules.agri.dto.ProductionJobDto;
import com.erp.platform.modules.agri.entity.AgriJobAllocation;
import com.erp.platform.modules.agri.entity.AgriProductionJob;
import com.erp.platform.modules.agri.repository.AgriJobAllocationRepository;
import com.erp.platform.modules.agri.repository.AgriProductionJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgriProductionJobService {

    private final AgriProductionJobRepository jobRepository;
    private final AgriJobAllocationRepository allocationRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionJobDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(jobRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(j -> toDto(j, loadAllocations(tenantId, j.getId()))));
    }

    public ProductionJobDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        AgriProductionJob job = jobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Production job not found: " + id));
        return toDto(job, loadAllocations(tenantId, id));
    }

    @Transactional
    public ProductionJobDto create(CreateProductionJobRequest req) {
        UUID tenantId = tenantContext.current();
        AgriProductionJob job = new AgriProductionJob();
        job.setTenantId(tenantId);
        applyRequest(job, req);
        job.setJobNumber(generateJobNumber(tenantId));
        AgriProductionJob saved = jobRepository.save(job);
        List<AgriJobAllocation> allocs = saveAllocations(tenantId, saved.getId(), saved.getJobNumber(), saved.getStatus(), req.getAllocations());
        return toDto(saved, allocs.stream().map(this::toAllocDto).collect(Collectors.toList()));
    }

    @Transactional
    public ProductionJobDto update(UUID id, CreateProductionJobRequest req) {
        UUID tenantId = tenantContext.current();
        AgriProductionJob job = jobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Production job not found: " + id));
        applyRequest(job, req);
        AgriProductionJob saved = jobRepository.save(job);
        List<AgriJobAllocation> allocs = saveAllocations(tenantId, saved.getId(), saved.getJobNumber(), saved.getStatus(), req.getAllocations());
        return toDto(saved, allocs.stream().map(this::toAllocDto).collect(Collectors.toList()));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        AgriProductionJob job = jobRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Production job not found: " + id));
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    private List<AgriJobAllocation> saveAllocations(UUID tenantId, UUID jobId, String jobNumber, String status,
                                                    List<AgriJobAllocationDto> dtos) {
        allocationRepository.deleteByTenantIdAndJobId(tenantId, jobId);
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        // Temp lot numbers are only generated once the job is INITIATED (or a later active status);
        // a DRAFT/CANCELLED job keeps its allocations lot-less. Existing lot numbers are always
        // preserved so re-saving a job is stable.
        boolean initiated = status != null
                && !status.equalsIgnoreCase("DRAFT")
                && !status.equalsIgnoreCase("CANCELLED");
        String lotBase = (jobNumber != null ? jobNumber.replaceFirst("^PJ-", "") : "TL");
        int[] seq = {0};
        List<AgriJobAllocation> allocs = dtos.stream().map(dto -> {
            seq[0]++;
            AgriJobAllocation a = new AgriJobAllocation();
            a.setTenantId(tenantId);
            a.setJobId(jobId);
            a.setAllocationType(dto.getAllocationType());
            a.setAllocateeId(dto.getAllocateeId());
            a.setAllocateeName(dto.getAllocateeName());
            a.setQuantityKgs(dto.getQuantityKgs());
            a.setAcreageAcres(dto.getAcreageAcres());
            a.setExpectedYieldKgs(dto.getExpectedYieldKgs());
            String lot = dto.getTempLotNumber();
            if ((lot == null || lot.isBlank()) && initiated) {
                lot = String.format("TL-%s-%02d", lotBase, seq[0]);
            }
            a.setTempLotNumber(lot);
            return a;
        }).collect(Collectors.toList());
        return allocationRepository.saveAll(allocs);
    }

    private List<AgriJobAllocationDto> loadAllocations(UUID tenantId, UUID jobId) {
        return allocationRepository.findByTenantIdAndJobIdAndDeletedAtIsNull(tenantId, jobId)
                .stream().map(this::toAllocDto).collect(Collectors.toList());
    }

    private String generateJobNumber(UUID tenantId) {
        long count = jobRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        return String.format("PJ-%d-%04d", Year.now().getValue(), count + 1);
    }

    private void applyRequest(AgriProductionJob e, CreateProductionJobRequest r) {
        e.setPlanId(r.getPlanId());
        e.setPlanNumber(r.getPlanNumber());
        e.setPlanName(r.getPlanName());
        e.setCropGroupId(r.getCropGroupId());
        e.setCropGroupName(r.getCropGroupName());
        e.setCropId(r.getCropId());
        e.setCropName(r.getCropName());
        e.setVarietyId(r.getVarietyId());
        e.setVarietyName(r.getVarietyName());
        e.setProductionAreaId(r.getProductionAreaId());
        e.setProductionAreaName(r.getProductionAreaName());
        e.setFromDate(r.getFromDate());
        e.setToDate(r.getToDate());
        e.setStatus(r.getStatus() != null ? r.getStatus() : "DRAFT");
        e.setNotes(r.getNotes());
    }

    private ProductionJobDto toDto(AgriProductionJob e, List<AgriJobAllocationDto> allocations) {
        ProductionJobDto dto = new ProductionJobDto();
        dto.setId(e.getId());
        dto.setJobNumber(e.getJobNumber());
        dto.setPlanId(e.getPlanId());
        dto.setPlanNumber(e.getPlanNumber());
        dto.setPlanName(e.getPlanName());
        dto.setCropGroupId(e.getCropGroupId());
        dto.setCropGroupName(e.getCropGroupName());
        dto.setCropId(e.getCropId());
        dto.setCropName(e.getCropName());
        dto.setVarietyId(e.getVarietyId());
        dto.setVarietyName(e.getVarietyName());
        dto.setProductionAreaId(e.getProductionAreaId());
        dto.setProductionAreaName(e.getProductionAreaName());
        dto.setFromDate(e.getFromDate());
        dto.setToDate(e.getToDate());
        dto.setStatus(e.getStatus());
        dto.setNotes(e.getNotes());
        dto.setAllocations(allocations);
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private AgriJobAllocationDto toAllocDto(AgriJobAllocation a) {
        AgriJobAllocationDto dto = new AgriJobAllocationDto();
        dto.setId(a.getId());
        dto.setAllocationType(a.getAllocationType());
        dto.setAllocateeId(a.getAllocateeId());
        dto.setAllocateeName(a.getAllocateeName());
        dto.setQuantityKgs(a.getQuantityKgs());
        dto.setAcreageAcres(a.getAcreageAcres());
        dto.setExpectedYieldKgs(a.getExpectedYieldKgs());
        dto.setTempLotNumber(a.getTempLotNumber());
        return dto;
    }
}
