package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.MaterialItemDto;
import com.erp.platform.modules.master.dto.MaterialItemRequest;
import com.erp.platform.modules.master.entity.MaterialItem;
import com.erp.platform.modules.master.repository.MaterialItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialItemService {

    private final MaterialItemRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<MaterialItemDto> list(Pageable pageable) {
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable)
                .map(this::toDto));
    }

    public List<MaterialItemDto> listByMaterial(UUID materialId) {
        return repository.findByTenantIdAndMaterialIdAndActiveTrueAndDeletedAtIsNull(
                        tenantContext.current(), materialId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<MaterialItemDto> listAll() {
        return repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public MaterialItemDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public MaterialItemDto create(MaterialItemRequest req) {
        UUID tenantId = tenantContext.current();
        if (req.getMaterialId() == null) throw AppException.badRequest("Material is required");
        if (StringUtils.hasText(req.getCode())
                && repository.existsByTenantIdAndMaterialIdAndCodeAndDeletedAtIsNull(
                        tenantId, req.getMaterialId(), req.getCode())) {
            throw AppException.conflict("Item code already exists for this material: " + req.getCode());
        }
        MaterialItem item = new MaterialItem();
        item.setTenantId(tenantId);
        applyRequest(item, req);
        return toDto(repository.save(item));
    }

    @Transactional
    public MaterialItemDto update(UUID id, MaterialItemRequest req) {
        MaterialItem item = findOrThrow(id);
        if (StringUtils.hasText(req.getCode()) && !req.getCode().equals(item.getCode())
                && repository.existsByTenantIdAndMaterialIdAndCodeAndDeletedAtIsNull(
                        item.getTenantId(), item.getMaterialId(), req.getCode())) {
            throw AppException.conflict("Item code already exists for this material: " + req.getCode());
        }
        applyRequest(item, req);
        return toDto(repository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        MaterialItem item = findOrThrow(id);
        item.setDeletedAt(LocalDateTime.now());
        repository.save(item);
    }

    private void applyRequest(MaterialItem item, MaterialItemRequest req) {
        if (req.getMaterialId() != null) item.setMaterialId(req.getMaterialId());
        if (req.getMaterialName() != null) item.setMaterialName(req.getMaterialName());
        if (req.getMaterialGroupId() != null) item.setMaterialGroupId(req.getMaterialGroupId());
        if (req.getMaterialGroupName() != null) item.setMaterialGroupName(req.getMaterialGroupName());
        item.setName(req.getName());
        item.setCode(req.getCode());
        item.setDescription(req.getDescription());
        item.setUnit(req.getUnit());
        item.setBatchWiseReceipt(req.isBatchWiseReceipt());
        item.setHasExpiryPeriod(req.isHasExpiryPeriod());
        item.setExpiryDate(req.getExpiryDate());
        item.setSellable(req.isSellable());
        item.setActive(req.isActive());
    }

    private MaterialItem findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Material item not found: " + id));
    }

    private MaterialItemDto toDto(MaterialItem item) {
        MaterialItemDto dto = new MaterialItemDto();
        dto.setId(item.getId());
        dto.setTenantId(item.getTenantId());
        dto.setMaterialId(item.getMaterialId());
        dto.setMaterialName(item.getMaterialName());
        dto.setMaterialGroupId(item.getMaterialGroupId());
        dto.setMaterialGroupName(item.getMaterialGroupName());
        dto.setName(item.getName());
        dto.setCode(item.getCode());
        dto.setDescription(item.getDescription());
        dto.setUnit(item.getUnit());
        dto.setBatchWiseReceipt(item.isBatchWiseReceipt());
        dto.setHasExpiryPeriod(item.isHasExpiryPeriod());
        dto.setExpiryDate(item.getExpiryDate());
        dto.setSellable(item.isSellable());
        dto.setActive(item.isActive());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }
}
