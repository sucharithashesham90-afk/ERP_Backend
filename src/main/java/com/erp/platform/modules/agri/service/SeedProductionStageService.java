package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedProductionStageRequest;
import com.erp.platform.modules.agri.dto.SeedProductionStageDto;
import com.erp.platform.modules.agri.entity.SeedProductionStage;
import com.erp.platform.modules.agri.repository.SeedProductionStageRepository;
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
public class SeedProductionStageService {

    private final SeedProductionStageRepository seedProductionStageRepository;
    private final TenantContext tenantContext;

    public PageResponse<SeedProductionStageDto> list(Pageable pageable) {
        return PageResponse.of(seedProductionStageRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SeedProductionStageDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedProductionStage entity = seedProductionStageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedProductionStage not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SeedProductionStageDto create(CreateSeedProductionStageRequest request) {
        UUID tenantId = tenantContext.current();
        SeedProductionStage entity = new SeedProductionStage();
        entity.setTenantId(tenantId);
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setRequiresApproval(request.isRequiresApproval());
        entity.setFromStage(request.getFromStage());
        entity.setToStage(request.getToStage());
        entity.setStageOrder(request.getStageOrder());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(seedProductionStageRepository.save(entity));
    }

    @Transactional
    public SeedProductionStageDto update(UUID id, CreateSeedProductionStageRequest request) {
        UUID tenantId = tenantContext.current();
        SeedProductionStage entity = seedProductionStageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedProductionStage not found: " + id));
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setRequiresApproval(request.isRequiresApproval());
        entity.setFromStage(request.getFromStage());
        entity.setToStage(request.getToStage());
        entity.setStageOrder(request.getStageOrder());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(seedProductionStageRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedProductionStage entity = seedProductionStageRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedProductionStage not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        seedProductionStageRepository.save(entity);
    }

    private SeedProductionStageDto toDto(SeedProductionStage entity) {
        SeedProductionStageDto dto = new SeedProductionStageDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setRequiresApproval(Boolean.TRUE.equals(entity.getRequiresApproval()));
        dto.setFromStage(entity.getFromStage());
        dto.setToStage(entity.getToStage());
        dto.setStageOrder(entity.getStageOrder());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

