package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.*;
import com.erp.platform.modules.accounting.entity.Dimension;
import com.erp.platform.modules.accounting.entity.DimensionValue;
import com.erp.platform.modules.accounting.repository.DimensionRepository;
import com.erp.platform.modules.accounting.repository.DimensionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DimensionService {

    private final DimensionRepository dimensionRepository;
    private final DimensionValueRepository dimensionValueRepository;
    private final TenantContext tenantContext;

    public PageResponse<DimensionDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<Dimension> page = dimensionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDtoWithoutValues));
    }

    public DimensionDto getById(UUID id) {
        Dimension dimension = findDimensionOrThrow(id);
        return toDtoWithValues(dimension);
    }

    @Transactional
    public DimensionDto create(CreateDimensionRequest request) {
        UUID tenantId = tenantContext.current();
        dimensionRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())
                .ifPresent(d -> { throw AppException.conflict("Dimension with code already exists: " + request.getCode()); });

        Dimension dimension = new Dimension();
        dimension.setTenantId(tenantId);
        dimension.setName(request.getName());
        dimension.setCode(request.getCode().toUpperCase());
        dimension.setDescription(request.getDescription());
        dimension.setMandatory(request.isMandatory());
        dimension.setActive(request.isActive());

        Dimension saved = dimensionRepository.save(dimension);
        log.info("Dimension created: id={}, code={}", saved.getId(), saved.getCode());
        return toDtoWithValues(saved);
    }

    @Transactional
    public DimensionDto update(UUID id, CreateDimensionRequest request) {
        Dimension dimension = findDimensionOrThrow(id);
        dimension.setName(request.getName());
        dimension.setDescription(request.getDescription());
        dimension.setMandatory(request.isMandatory());
        dimension.setActive(request.isActive());
        Dimension saved = dimensionRepository.save(dimension);
        log.info("Dimension updated: id={}", id);
        return toDtoWithValues(saved);
    }

    @Transactional
    public DimensionValueDto addValue(UUID dimensionId, CreateDimensionValueRequest request) {
        UUID tenantId = tenantContext.current();
        Dimension dimension = findDimensionOrThrow(dimensionId);

        DimensionValue value = new DimensionValue();
        value.setTenantId(tenantId);
        value.setDimension(dimension);
        value.setCode(request.getCode());
        value.setName(request.getName());
        value.setDescription(request.getDescription());
        value.setParentId(request.getParentId());
        value.setActive(request.isActive());

        DimensionValue saved = dimensionValueRepository.save(value);
        log.info("DimensionValue created: id={}, code={}", saved.getId(), saved.getCode());
        return toValueDto(saved);
    }

    @Transactional
    public DimensionValueDto updateValue(UUID valueId, CreateDimensionValueRequest request) {
        UUID tenantId = tenantContext.current();
        DimensionValue value = dimensionValueRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, valueId)
                .orElseThrow(() -> AppException.notFound("Dimension value not found: " + valueId));
        value.setName(request.getName());
        value.setDescription(request.getDescription());
        value.setParentId(request.getParentId());
        value.setActive(request.isActive());
        DimensionValue saved = dimensionValueRepository.save(value);
        log.info("DimensionValue updated: id={}", valueId);
        return toValueDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Dimension dimension = findDimensionOrThrow(id);
        dimension.setDeletedAt(LocalDateTime.now());
        dimensionRepository.save(dimension);
        log.info("Dimension soft-deleted: id={}", id);
    }

    public PageResponse<DimensionValueDto> listValues(UUID dimensionId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Dimension dimension = findDimensionOrThrow(dimensionId);
        Page<DimensionValue> page = dimensionValueRepository
                .findByTenantIdAndDimensionAndDeletedAtIsNull(tenantId, dimension, pageable);
        return PageResponse.of(page.map(this::toValueDto));
    }

    private Dimension findDimensionOrThrow(UUID id) {
        return dimensionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Dimension not found: " + id));
    }

    private DimensionDto toDtoWithoutValues(Dimension d) {
        DimensionDto dto = new DimensionDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setDescription(d.getDescription());
        dto.setMandatory(d.isMandatory());
        dto.setActive(d.isActive());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }

    private DimensionDto toDtoWithValues(Dimension d) {
        DimensionDto dto = toDtoWithoutValues(d);
        List<DimensionValueDto> values = d.getValues().stream()
                .filter(v -> v.getDeletedAt() == null)
                .map(this::toValueDto)
                .collect(Collectors.toList());
        dto.setValues(values);
        return dto;
    }

    private DimensionValueDto toValueDto(DimensionValue v) {
        DimensionValueDto dto = new DimensionValueDto();
        dto.setId(v.getId());
        dto.setDimensionId(v.getDimension().getId());
        dto.setDimensionCode(v.getDimension().getCode());
        dto.setCode(v.getCode());
        dto.setName(v.getName());
        dto.setDescription(v.getDescription());
        dto.setParentId(v.getParentId());
        dto.setActive(v.isActive());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        return dto;
    }
}
