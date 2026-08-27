package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedStateRequest;
import com.erp.platform.modules.agri.dto.SeedStateDto;
import com.erp.platform.modules.agri.entity.SeedState;
import com.erp.platform.modules.agri.repository.SeedStateRepository;
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
public class SeedStateService {

    private final SeedStateRepository seedStateRepository;
    private final TenantContext tenantContext;

    public PageResponse<SeedStateDto> list(Pageable pageable) {
        return PageResponse.of(seedStateRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SeedStateDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedState entity = seedStateRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedState not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SeedStateDto create(CreateSeedStateRequest request) {
        UUID tenantId = tenantContext.current();
        SeedState entity = new SeedState();
        entity.setTenantId(tenantId);
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setSortOrder(request.getSortOrder());
        entity.setActive(request.isActive());
        return toDto(seedStateRepository.save(entity));
    }

    @Transactional
    public SeedStateDto update(UUID id, CreateSeedStateRequest request) {
        UUID tenantId = tenantContext.current();
        SeedState entity = seedStateRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedState not found: " + id));
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setSortOrder(request.getSortOrder());
        entity.setActive(request.isActive());
        return toDto(seedStateRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedState entity = seedStateRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedState not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        seedStateRepository.save(entity);
    }

    private SeedStateDto toDto(SeedState entity) {
        SeedStateDto dto = new SeedStateDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setSortOrder(entity.getSortOrder());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

