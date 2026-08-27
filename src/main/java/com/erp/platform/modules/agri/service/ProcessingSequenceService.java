package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessingSequenceRequest;
import com.erp.platform.modules.agri.dto.ProcessingSequenceDto;
import com.erp.platform.modules.agri.entity.ProcessingSequence;
import com.erp.platform.modules.agri.repository.ProcessingSequenceRepository;
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
public class ProcessingSequenceService {

    private final ProcessingSequenceRepository processingSequenceRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessingSequenceDto> list(Pageable pageable) {
        return PageResponse.of(processingSequenceRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ProcessingSequenceDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessingSequence entity = processingSequenceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessingSequence not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ProcessingSequenceDto create(CreateProcessingSequenceRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessingSequence entity = new ProcessingSequence();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setProcessingSteps(request.getProcessingSteps());
        entity.setStepsJson(request.getStepsJson());
        entity.setActive(request.isActive());
        return toDto(processingSequenceRepository.save(entity));
    }

    @Transactional
    public ProcessingSequenceDto update(UUID id, CreateProcessingSequenceRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessingSequence entity = processingSequenceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessingSequence not found: " + id));
        entity.setName(request.getName());
        entity.setProcessingSteps(request.getProcessingSteps());
        entity.setStepsJson(request.getStepsJson());
        entity.setActive(request.isActive());
        return toDto(processingSequenceRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessingSequence entity = processingSequenceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessingSequence not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        processingSequenceRepository.save(entity);
    }

    private ProcessingSequenceDto toDto(ProcessingSequence entity) {
        ProcessingSequenceDto dto = new ProcessingSequenceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setProcessingSteps(entity.getProcessingSteps());
        dto.setStepsJson(entity.getStepsJson());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

