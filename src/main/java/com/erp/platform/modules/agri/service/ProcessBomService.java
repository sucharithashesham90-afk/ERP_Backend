package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessBomRequest;
import com.erp.platform.modules.agri.dto.ProcessBomDto;
import com.erp.platform.modules.agri.entity.ProcessBom;
import com.erp.platform.modules.agri.repository.ProcessBomRepository;
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
public class ProcessBomService {

    private final ProcessBomRepository processBomRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessBomDto> list(Pageable pageable) {
        return PageResponse.of(processBomRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ProcessBomDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessBom entity = processBomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessBom not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ProcessBomDto create(CreateProcessBomRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessBom entity = new ProcessBom();
        entity.setTenantId(tenantId);
        entity.setProcessingType(request.getProcessingType());
        entity.setItemCode(request.getItemCode());
        entity.setItemName(request.getItemName());
        entity.setQuantity(request.getQuantity());
        entity.setUnit(request.getUnit());
        entity.setBomType(request.getBomType());
        entity.setActive(request.isActive());
        return toDto(processBomRepository.save(entity));
    }

    @Transactional
    public ProcessBomDto update(UUID id, CreateProcessBomRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessBom entity = processBomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessBom not found: " + id));
        entity.setProcessingType(request.getProcessingType());
        entity.setItemCode(request.getItemCode());
        entity.setItemName(request.getItemName());
        entity.setQuantity(request.getQuantity());
        entity.setUnit(request.getUnit());
        entity.setBomType(request.getBomType());
        entity.setActive(request.isActive());
        return toDto(processBomRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessBom entity = processBomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessBom not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        processBomRepository.save(entity);
    }

    private ProcessBomDto toDto(ProcessBom entity) {
        ProcessBomDto dto = new ProcessBomDto();
        dto.setId(entity.getId());
        dto.setProcessingType(entity.getProcessingType());
        dto.setItemCode(entity.getItemCode());
        dto.setItemName(entity.getItemName());
        dto.setQuantity(entity.getQuantity());
        dto.setUnit(entity.getUnit());
        dto.setBomType(entity.getBomType());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

