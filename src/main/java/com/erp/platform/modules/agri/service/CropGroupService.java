package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateCropGroupRequest;
import com.erp.platform.modules.agri.dto.CropGroupDto;
import com.erp.platform.modules.agri.entity.CropGroup;
import com.erp.platform.modules.agri.repository.CropGroupRepository;
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
public class CropGroupService {

    private final CropGroupRepository cropGroupRepository;
    private final TenantContext tenantContext;

    public PageResponse<CropGroupDto> list(Pageable pageable) {
        return PageResponse.of(cropGroupRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public List<CropGroupDto> listAll() {
        return cropGroupRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public CropGroupDto getById(UUID id) {
        CropGroup entity = cropGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("CropGroup not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public CropGroupDto create(CreateCropGroupRequest request) {
        CropGroup entity = new CropGroup();
        entity.setTenantId(tenantContext.current());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toDto(cropGroupRepository.save(entity));
    }

    @Transactional
    public CropGroupDto update(UUID id, CreateCropGroupRequest request) {
        CropGroup entity = cropGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("CropGroup not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toDto(cropGroupRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        CropGroup entity = cropGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("CropGroup not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        cropGroupRepository.save(entity);
    }

    private CropGroupDto toDto(CropGroup e) {
        CropGroupDto dto = new CropGroupDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
