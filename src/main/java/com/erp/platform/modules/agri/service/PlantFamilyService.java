package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePlantFamilyRequest;
import com.erp.platform.modules.agri.dto.PlantFamilyDto;
import com.erp.platform.modules.agri.entity.PlantFamily;
import com.erp.platform.modules.agri.repository.PlantFamilyRepository;
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
public class PlantFamilyService {

    private final PlantFamilyRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<PlantFamilyDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<PlantFamily> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<PlantFamilyDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<PlantFamilyDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public PlantFamilyDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PlantFamilyDto create(CreatePlantFamilyRequest req) {
        PlantFamily entity = new PlantFamily();
        entity.setTenantId(tenantContext.current());
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setScientificName(req.getScientificName());
        entity.setActive(true);
        entity = repository.save(entity);
        log.info("PlantFamily created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PlantFamilyDto update(UUID id, CreatePlantFamilyRequest req) {
        PlantFamily entity = findOrThrow(id);
        entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getScientificName() != null) entity.setScientificName(req.getScientificName());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PlantFamily entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PlantFamily findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plant family not found: " + id));
    }

    private PlantFamilyDto toDto(PlantFamily e) {
        PlantFamilyDto dto = new PlantFamilyDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setDescription(e.getDescription());
        dto.setScientificName(e.getScientificName());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
