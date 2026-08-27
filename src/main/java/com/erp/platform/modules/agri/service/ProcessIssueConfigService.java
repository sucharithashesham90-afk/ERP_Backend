package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessIssueConfigRequest;
import com.erp.platform.modules.agri.dto.ProcessIssueConfigDto;
import com.erp.platform.modules.agri.entity.ProcessIssueConfig;
import com.erp.platform.modules.agri.repository.ProcessIssueConfigRepository;
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
public class ProcessIssueConfigService {

    private final ProcessIssueConfigRepository processIssueConfigRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProcessIssueConfigDto> list(Pageable pageable) {
        return PageResponse.of(processIssueConfigRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ProcessIssueConfigDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessIssueConfig entity = processIssueConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessIssueConfig not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ProcessIssueConfigDto create(CreateProcessIssueConfigRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessIssueConfig entity = new ProcessIssueConfig();
        entity.setTenantId(tenantId);
        entity.setProcessType(request.getProcessType());
        entity.setByproductName(request.getByproductName());
        entity.setEnabled(request.isEnabled());
        entity.setIssueCategory(request.getIssueCategory());
        return toDto(processIssueConfigRepository.save(entity));
    }

    @Transactional
    public ProcessIssueConfigDto update(UUID id, CreateProcessIssueConfigRequest request) {
        UUID tenantId = tenantContext.current();
        ProcessIssueConfig entity = processIssueConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessIssueConfig not found: " + id));
        entity.setProcessType(request.getProcessType());
        entity.setByproductName(request.getByproductName());
        entity.setEnabled(request.isEnabled());
        entity.setIssueCategory(request.getIssueCategory());
        return toDto(processIssueConfigRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ProcessIssueConfig entity = processIssueConfigRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ProcessIssueConfig not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        processIssueConfigRepository.save(entity);
    }

    private ProcessIssueConfigDto toDto(ProcessIssueConfig entity) {
        ProcessIssueConfigDto dto = new ProcessIssueConfigDto();
        dto.setId(entity.getId());
        dto.setProcessType(entity.getProcessType());
        dto.setByproductName(entity.getByproductName());
        dto.setEnabled(entity.isEnabled());
        dto.setIssueCategory(entity.getIssueCategory());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

