package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateUoMRequest;
import com.erp.platform.modules.master.dto.UnitOfMeasureDto;
import com.erp.platform.modules.master.entity.UnitOfMeasure;
import com.erp.platform.modules.master.entity.UnitOfMeasure.UoMType;
import com.erp.platform.modules.master.repository.UoMConversionRepository;
import com.erp.platform.modules.master.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository uomRepository;
    private final UoMConversionRepository conversionRepository;
    private final TenantContext tenantContext;

    public PageResponse<UnitOfMeasureDto> list(UoMType type, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<UnitOfMeasure> page = (type != null)
                ? uomRepository.findByTenantIdAndUomTypeAndDeletedAtIsNull(tenantId, type, pageable)
                : uomRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public List<UnitOfMeasureDto> all() {
        UUID tenantId = tenantContext.current();
        return uomRepository.findByTenantIdAndDeletedAtIsNullOrderByCodeAsc(tenantId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public UnitOfMeasureDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public UnitOfMeasureDto create(CreateUoMRequest request) {
        UUID tenantId = tenantContext.current();
        uomRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())
                .ifPresent(e -> { throw AppException.conflict("UoM with code already exists: " + request.getCode()); });

        UnitOfMeasure uom = new UnitOfMeasure();
        uom.setTenantId(tenantId);
        uom.setCode(request.getCode().toUpperCase());
        uom.setName(request.getName());
        uom.setSymbol(request.getSymbol());
        uom.setUomType(request.getUomType());
        uom.setBaseUnit(request.isBaseUnit());
        uom.setDecimalPlaces(request.getDecimalPlaces());
        uom.setActive(request.isActive());
        uom.setNotes(request.getNotes());

        uom = uomRepository.save(uom);
        log.info("UoM created: id={}, code={}", uom.getId(), uom.getCode());
        return toDto(uom);
    }

    @Transactional
    public UnitOfMeasureDto update(UUID id, CreateUoMRequest request) {
        UnitOfMeasure uom = findOrThrow(id);
        UUID tenantId = tenantContext.current();

        if (!uom.getCode().equalsIgnoreCase(request.getCode())) {
            uomRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())
                    .ifPresent(e -> { throw AppException.conflict("UoM with code already exists: " + request.getCode()); });
            uom.setCode(request.getCode().toUpperCase());
        }

        uom.setName(request.getName());
        uom.setSymbol(request.getSymbol());
        uom.setUomType(request.getUomType());
        uom.setBaseUnit(request.isBaseUnit());
        uom.setDecimalPlaces(request.getDecimalPlaces());
        uom.setActive(request.isActive());
        uom.setNotes(request.getNotes());

        return toDto(uomRepository.save(uom));
    }

    @Transactional
    public void delete(UUID id) {
        UnitOfMeasure uom = findOrThrow(id);
        uom.setDeletedAt(LocalDateTime.now());
        uomRepository.save(uom);
        log.info("UoM soft-deleted: id={}", id);
    }

    public BigDecimal convert(UUID fromUomId, UUID toUomId, BigDecimal qty) {
        UUID tenantId = tenantContext.current();
        var conversion = conversionRepository.findByTenantIdAndFromUomIdAndToUomIdAndDeletedAtIsNull(tenantId, fromUomId, toUomId)
                .orElseThrow(() -> AppException.notFound("No conversion found from UoM " + fromUomId + " to " + toUomId));
        return qty.multiply(conversion.getConversionFactor());
    }

    private UnitOfMeasure findOrThrow(UUID id) {
        return uomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("UoM not found: " + id));
    }

    private UnitOfMeasureDto toDto(UnitOfMeasure uom) {
        UnitOfMeasureDto dto = new UnitOfMeasureDto();
        dto.setId(uom.getId());
        dto.setTenantId(uom.getTenantId());
        dto.setCode(uom.getCode());
        dto.setName(uom.getName());
        dto.setSymbol(uom.getSymbol());
        dto.setUomType(uom.getUomType());
        dto.setBaseUnit(uom.isBaseUnit());
        dto.setDecimalPlaces(uom.getDecimalPlaces());
        dto.setActive(uom.isActive());
        dto.setNotes(uom.getNotes());
        dto.setCreatedAt(uom.getCreatedAt());
        return dto;
    }
}
