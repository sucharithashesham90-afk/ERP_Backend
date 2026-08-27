package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePlantProcessSequenceRequest;
import com.erp.platform.modules.agri.dto.PlantProcessSequenceDto;
import com.erp.platform.modules.agri.entity.PlantProcessSequence;
import com.erp.platform.modules.agri.repository.PlantCategoryRepository;
import com.erp.platform.modules.agri.repository.PlantProcessSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlantProcessSequenceService {

    private final PlantProcessSequenceRepository repository;
    private final PlantCategoryRepository categoryRepository;
    private final TenantContext tenantContext;

    public PageResponse<PlantProcessSequenceDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<PlantProcessSequence> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNullOrderBySequenceOrder(tenantId);
        List<PlantProcessSequenceDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<PlantProcessSequenceDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public PlantProcessSequenceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PlantProcessSequenceDto create(CreatePlantProcessSequenceRequest req) {
        UUID tenantId = tenantContext.current();
        PlantProcessSequence entity = new PlantProcessSequence();
        entity.setTenantId(tenantId);
        applyRequest(entity, req, tenantId);
        entity.setActive(true);
        return toDto(repository.save(entity));
    }

    @Transactional
    public PlantProcessSequenceDto update(UUID id, CreatePlantProcessSequenceRequest req) {
        UUID tenantId = tenantContext.current();
        PlantProcessSequence entity = findOrThrow(id);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PlantProcessSequence entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(PlantProcessSequence e, CreatePlantProcessSequenceRequest req, UUID tenantId) {
        e.setSequenceOrder(req.getSequenceOrder());
        e.setProcessStepName(req.getProcessStepName());
        e.setDescription(req.getDescription());
        e.setProcessType(req.getProcessType());
        e.setExpectedOutputPercent(req.getExpectedOutputPercent());
        if (req.getPlantCategoryId() != null) {
            categoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantCategoryId())
                    .ifPresent(e::setPlantCategory);
        }
    }

    private PlantProcessSequence findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plant process sequence not found: " + id));
    }

    private PlantProcessSequenceDto toDto(PlantProcessSequence e) {
        PlantProcessSequenceDto dto = new PlantProcessSequenceDto();
        dto.setId(e.getId());
        dto.setSequenceOrder(e.getSequenceOrder());
        dto.setProcessStepName(e.getProcessStepName());
        dto.setDescription(e.getDescription());
        dto.setProcessType(e.getProcessType());
        dto.setExpectedOutputPercent(e.getExpectedOutputPercent());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getPlantCategory() != null) {
            dto.setPlantCategoryId(e.getPlantCategory().getId());
            dto.setPlantCategoryName(e.getPlantCategory().getName());
        }
        return dto;
    }
}
