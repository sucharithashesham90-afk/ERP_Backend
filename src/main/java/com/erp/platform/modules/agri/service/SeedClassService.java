package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedClassRequest;
import com.erp.platform.modules.agri.dto.SeedClassDto;
import com.erp.platform.modules.agri.entity.SeedClass;
import com.erp.platform.modules.agri.repository.SeedClassRepository;
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
public class SeedClassService {

    private final SeedClassRepository seedClassRepository;
    private final TenantContext tenantContext;

    public PageResponse<SeedClassDto> list(Pageable pageable) {
        return PageResponse.of(seedClassRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public SeedClassDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedClass entity = seedClassRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedClass not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SeedClassDto create(CreateSeedClassRequest request) {
        UUID tenantId = tenantContext.current();
        SeedClass entity = new SeedClass();
        entity.setTenantId(tenantId);
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setMinimumPurity(request.getMinimumPurity());
        entity.setMinimumGermination(request.getMinimumGermination());
        entity.setActive(request.isActive());
        return toDto(seedClassRepository.save(entity));
    }

    @Transactional
    public SeedClassDto update(UUID id, CreateSeedClassRequest request) {
        UUID tenantId = tenantContext.current();
        SeedClass entity = seedClassRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedClass not found: " + id));
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setMinimumPurity(request.getMinimumPurity());
        entity.setMinimumGermination(request.getMinimumGermination());
        entity.setActive(request.isActive());
        return toDto(seedClassRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SeedClass entity = seedClassRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeedClass not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        seedClassRepository.save(entity);
    }

    private SeedClassDto toDto(SeedClass entity) {
        SeedClassDto dto = new SeedClassDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setMinimumPurity(entity.getMinimumPurity());
        dto.setMinimumGermination(entity.getMinimumGermination());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

