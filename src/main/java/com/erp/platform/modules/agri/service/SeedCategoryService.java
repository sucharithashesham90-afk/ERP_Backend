package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedCategoryRequest;
import com.erp.platform.modules.agri.dto.SeedCategoryDto;
import com.erp.platform.modules.agri.entity.SeedCategory;
import com.erp.platform.modules.agri.repository.SeedCategoryRepository;
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
public class SeedCategoryService {

    private final SeedCategoryRepository seedCategoryRepository;
    private final TenantContext tenantContext;

    public PageResponse<SeedCategoryDto> list(Pageable pageable) {
        return PageResponse.of(seedCategoryRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SeedCategoryDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedCategory entity = seedCategoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedCategory not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SeedCategoryDto create(CreateSeedCategoryRequest request) {
        UUID tenantId = tenantContext.current();
        SeedCategory entity = new SeedCategory();
        entity.setTenantId(tenantId);
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(seedCategoryRepository.save(entity));
    }

    @Transactional
    public SeedCategoryDto update(UUID id, CreateSeedCategoryRequest request) {
        UUID tenantId = tenantContext.current();
        SeedCategory entity = seedCategoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedCategory not found: " + id));
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(seedCategoryRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedCategory entity = seedCategoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedCategory not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        seedCategoryRepository.save(entity);
    }

    private SeedCategoryDto toDto(SeedCategory entity) {
        SeedCategoryDto dto = new SeedCategoryDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

