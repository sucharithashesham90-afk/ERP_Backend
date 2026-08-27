package com.erp.platform.modules.master.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.MaterialGroupDto;
import com.erp.platform.modules.master.dto.MaterialGroupRequest;
import com.erp.platform.modules.master.entity.MaterialGroup;
import com.erp.platform.modules.master.repository.MaterialGroupRepository;
import lombok.RequiredArgsConstructor;
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
public class MaterialGroupService {

    private final MaterialGroupRepository repository;
    private final TenantContext tenantContext;

    public List<MaterialGroupDto> list() {
        return repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public MaterialGroupDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public MaterialGroupDto create(MaterialGroupRequest req) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(req.getCode())
                && repository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, req.getCode())) {
            throw AppException.conflict("Material group code already exists: " + req.getCode());
        }
        MaterialGroup g = new MaterialGroup();
        g.setTenantId(tenantId);
        g.setName(req.getName());
        g.setCode(req.getCode());
        g.setDescription(req.getDescription());
        applyExtras(g, req);
        g.setActive(req.isActive());
        return toDto(repository.save(g));
    }

    @Transactional
    public MaterialGroupDto update(UUID id, MaterialGroupRequest req) {
        MaterialGroup g = findOrThrow(id);
        if (StringUtils.hasText(req.getCode()) && !req.getCode().equals(g.getCode())
                && repository.existsByTenantIdAndCodeAndDeletedAtIsNull(g.getTenantId(), req.getCode())) {
            throw AppException.conflict("Material group code already exists: " + req.getCode());
        }
        g.setName(req.getName());
        g.setCode(req.getCode());
        g.setDescription(req.getDescription());
        applyExtras(g, req);
        g.setActive(req.isActive());
        return toDto(repository.save(g));
    }

    /** Copy the extended (documented) fields from request onto the entity. */
    private void applyExtras(MaterialGroup g, MaterialGroupRequest req) {
        g.setParentGroupId(req.getParentGroupId());
        g.setParentGroupName(req.getParentGroupName());
        g.setBatchWiseReceipt(req.isBatchWiseReceipt());
        g.setHasExpiryPeriod(req.isHasExpiryPeriod());
        g.setExpiryDate(req.getExpiryDate());
        g.setSellable(req.isSellable());
        g.setUomCategoryId(req.getUomCategoryId());
        g.setUomCategoryName(req.getUomCategoryName());
    }

    @Transactional
    public void delete(UUID id) {
        MaterialGroup g = findOrThrow(id);
        g.setDeletedAt(LocalDateTime.now());
        repository.save(g);
    }

    private MaterialGroup findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Material group not found: " + id));
    }

    private MaterialGroupDto toDto(MaterialGroup g) {
        MaterialGroupDto dto = new MaterialGroupDto();
        dto.setId(g.getId());
        dto.setTenantId(g.getTenantId());
        dto.setName(g.getName());
        dto.setCode(g.getCode());
        dto.setDescription(g.getDescription());
        dto.setParentGroupId(g.getParentGroupId());
        dto.setParentGroupName(g.getParentGroupName());
        dto.setBatchWiseReceipt(g.isBatchWiseReceipt());
        dto.setHasExpiryPeriod(g.isHasExpiryPeriod());
        dto.setExpiryDate(g.getExpiryDate());
        dto.setSellable(g.isSellable());
        dto.setUomCategoryId(g.getUomCategoryId());
        dto.setUomCategoryName(g.getUomCategoryName());
        dto.setActive(g.isActive());
        dto.setCreatedAt(g.getCreatedAt());
        return dto;
    }
}
