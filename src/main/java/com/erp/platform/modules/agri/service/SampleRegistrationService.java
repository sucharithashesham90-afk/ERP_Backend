package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSampleRegistrationRequest;
import com.erp.platform.modules.agri.dto.SampleRegistrationDto;
import com.erp.platform.modules.agri.entity.SampleRegistration;
import com.erp.platform.modules.agri.repository.SampleRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SampleRegistrationService {

    private final SampleRegistrationRepository sampleRegistrationRepository;
    private final TenantContext tenantContext;

    public PageResponse<SampleRegistrationDto> list(Pageable pageable) {
        return PageResponse.of(sampleRegistrationRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SampleRegistrationDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SampleRegistration entity = sampleRegistrationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SampleRegistration not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SampleRegistrationDto create(CreateSampleRegistrationRequest request) {
        UUID tenantId = tenantContext.current();
        SampleRegistration entity = new SampleRegistration();
        entity.setTenantId(tenantId);
        mapFields(entity, request);
        return toDto(sampleRegistrationRepository.save(entity));
    }

    @Transactional
    public SampleRegistrationDto update(UUID id, CreateSampleRegistrationRequest request) {
        UUID tenantId = tenantContext.current();
        SampleRegistration entity = sampleRegistrationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SampleRegistration not found: " + id));
        mapFields(entity, request);
        return toDto(sampleRegistrationRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SampleRegistration entity = sampleRegistrationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SampleRegistration not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        sampleRegistrationRepository.save(entity);
    }

    @Transactional
    public List<SampleRegistrationDto> createBatch(List<UUID> ids) {
        UUID tenantId = tenantContext.current();
        List<SampleRegistration> samples = sampleRegistrationRepository
                .findByTenantIdAndIdInAndDeletedAtIsNull(tenantId, ids);
        if (samples.isEmpty()) throw new RuntimeException("No valid samples found");

        String batchNumber = "BATCH-" + System.currentTimeMillis();
        samples.forEach(s -> s.setBatchNumber(batchNumber));
        return sampleRegistrationRepository.saveAll(samples).stream().map(this::toDto).collect(Collectors.toList());
    }

    private void mapFields(SampleRegistration entity, CreateSampleRegistrationRequest request) {
        entity.setSampleNumber(request.getSampleNumber());
        entity.setBatchNumber(request.getBatchNumber());
        entity.setLotNumber(request.getLotNumber());
        entity.setRegistrationNumber(request.getRegistrationNumber());
        entity.setCropGroupId(request.getCropGroupId());
        entity.setCropGroupName(request.getCropGroupName());
        entity.setCropId(request.getCropId());
        entity.setCropName(request.getCropName());
        entity.setVarietyId(request.getVarietyId());
        entity.setVarietyName(request.getVarietyName());
        entity.setSeedStateId(request.getSeedStateId());
        entity.setSeedStateName(request.getSeedStateName());
        entity.setCropVarietyTestId(request.getCropVarietyTestId());
        entity.setSampleDate(request.getSampleDate());
        entity.setTestLocationId(request.getTestLocationId());
        entity.setTestLocationName(request.getTestLocationName());
        entity.setSampleWeightGrams(request.getSampleWeightGrams());
        entity.setSubmittedBy(request.getSubmittedBy());
        entity.setStatus(request.getStatus());
        entity.setRemarks(request.getRemarks());
    }

    private SampleRegistrationDto toDto(SampleRegistration entity) {
        SampleRegistrationDto dto = new SampleRegistrationDto();
        dto.setId(entity.getId());
        dto.setSampleNumber(entity.getSampleNumber());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setLotNumber(entity.getLotNumber());
        dto.setRegistrationNumber(entity.getRegistrationNumber());
        dto.setCropGroupId(entity.getCropGroupId());
        dto.setCropGroupName(entity.getCropGroupName());
        dto.setCropId(entity.getCropId());
        dto.setCropName(entity.getCropName());
        dto.setVarietyId(entity.getVarietyId());
        dto.setVarietyName(entity.getVarietyName());
        dto.setSeedStateId(entity.getSeedStateId());
        dto.setSeedStateName(entity.getSeedStateName());
        dto.setCropVarietyTestId(entity.getCropVarietyTestId());
        dto.setSampleDate(entity.getSampleDate());
        dto.setTestLocationId(entity.getTestLocationId());
        dto.setTestLocationName(entity.getTestLocationName());
        dto.setSampleWeightGrams(entity.getSampleWeightGrams());
        dto.setSubmittedBy(entity.getSubmittedBy());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
