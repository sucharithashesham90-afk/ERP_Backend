package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePlantCategoryRequest;
import com.erp.platform.modules.agri.dto.PlantCategoryDto;
import com.erp.platform.modules.agri.entity.PlantCategory;
import com.erp.platform.modules.agri.entity.PlantFamily;
import com.erp.platform.modules.agri.repository.PlantCategoryRepository;
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
public class PlantCategoryService {

    private final PlantCategoryRepository repository;
    private final PlantFamilyRepository familyRepository;
    private final TenantContext tenantContext;

    public PageResponse<PlantCategoryDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<PlantCategory> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<PlantCategoryDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<PlantCategoryDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public List<PlantCategoryDto> listByFamily(UUID familyId) {
        return repository.findByTenantIdAndPlantFamilyIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current(), familyId)
                .stream().map(this::toDto).toList();
    }

    public PlantCategoryDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PlantCategoryDto create(CreatePlantCategoryRequest req) {
        UUID tenantId = tenantContext.current();
        PlantCategory entity = new PlantCategory();
        entity.setTenantId(tenantId);
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setScientificName(req.getScientificName());
        entity.setDefaultSeason(req.getDefaultSeason());
        entity.setActive(true);
        if (req.getPlantFamilyId() != null) {
            PlantFamily family = familyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantFamilyId())
                    .orElse(null);
            entity.setPlantFamily(family);
        }
        entity = repository.save(entity);
        log.info("PlantCategory created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PlantCategoryDto update(UUID id, CreatePlantCategoryRequest req) {
        UUID tenantId = tenantContext.current();
        PlantCategory entity = findOrThrow(id);
        entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getScientificName() != null) entity.setScientificName(req.getScientificName());
        if (req.getDefaultSeason() != null) entity.setDefaultSeason(req.getDefaultSeason());
        if (req.getPlantFamilyId() != null) {
            PlantFamily family = familyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantFamilyId())
                    .orElse(null);
            entity.setPlantFamily(family);
        } else {
            entity.setPlantFamily(null);
        }
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PlantCategory entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PlantCategory findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plant category not found: " + id));
    }

    private PlantCategoryDto toDto(PlantCategory e) {
        PlantCategoryDto dto = new PlantCategoryDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setDescription(e.getDescription());
        dto.setScientificName(e.getScientificName());
        dto.setDefaultSeason(e.getDefaultSeason());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getPlantFamily() != null) {
            dto.setPlantFamilyId(e.getPlantFamily().getId());
            dto.setPlantFamilyName(e.getPlantFamily().getName());
        }
        return dto;
    }
}
