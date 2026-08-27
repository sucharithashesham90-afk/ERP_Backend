package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateWasteTypeRequest;
import com.erp.platform.modules.agri.dto.WasteTypeDto;
import com.erp.platform.modules.agri.entity.WasteType;
import com.erp.platform.modules.agri.repository.WasteTypeRepository;
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
public class WasteTypeService {

    private final WasteTypeRepository wasteTypeRepository;
    private final TenantContext tenantContext;

    public PageResponse<WasteTypeDto> list(Pageable pageable) {
        return PageResponse.of(wasteTypeRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public WasteTypeDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        WasteType entity = wasteTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("WasteType not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public WasteTypeDto create(CreateWasteTypeRequest request) {
        UUID tenantId = tenantContext.current();
        WasteType entity = new WasteType();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(wasteTypeRepository.save(entity));
    }

    @Transactional
    public WasteTypeDto update(UUID id, CreateWasteTypeRequest request) {
        UUID tenantId = tenantContext.current();
        WasteType entity = wasteTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("WasteType not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setActive(request.isActive());
        return toDto(wasteTypeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        WasteType entity = wasteTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("WasteType not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        wasteTypeRepository.save(entity);
    }

    private WasteTypeDto toDto(WasteType entity) {
        WasteTypeDto dto = new WasteTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}

