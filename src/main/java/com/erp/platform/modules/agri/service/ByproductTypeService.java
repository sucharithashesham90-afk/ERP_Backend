package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.ByproductTypeDto;
import com.erp.platform.modules.agri.dto.CreateByproductTypeRequest;
import com.erp.platform.modules.agri.entity.ByproductType;
import com.erp.platform.modules.agri.repository.ByproductTypeRepository;
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
public class ByproductTypeService {

    private final ByproductTypeRepository byproductTypeRepository;
    private final TenantContext tenantContext;

    public PageResponse<ByproductTypeDto> list(Pageable pageable) {
        return PageResponse.of(byproductTypeRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ByproductTypeDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ByproductType entity = byproductTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ByproductType not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ByproductTypeDto create(CreateByproductTypeRequest request) {
        UUID tenantId = tenantContext.current();
        ByproductType entity = new ByproductType();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(byproductTypeRepository.save(entity));
    }

    @Transactional
    public ByproductTypeDto update(UUID id, CreateByproductTypeRequest request) {
        UUID tenantId = tenantContext.current();
        ByproductType entity = byproductTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ByproductType not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(byproductTypeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ByproductType entity = byproductTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ByproductType not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        byproductTypeRepository.save(entity);
    }

    private ByproductTypeDto toDto(ByproductType entity) {
        ByproductTypeDto dto = new ByproductTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

