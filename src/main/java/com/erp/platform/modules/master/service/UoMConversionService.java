package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateUoMConversionRequest;
import com.erp.platform.modules.master.dto.UoMConversionDto;
import com.erp.platform.modules.master.entity.UoMConversion;
import com.erp.platform.modules.master.entity.UnitOfMeasure;
import com.erp.platform.modules.master.repository.UoMConversionRepository;
import com.erp.platform.modules.master.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UoMConversionService {

    private final UoMConversionRepository conversionRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final TenantContext tenantContext;

    public PageResponse<UoMConversionDto> list(UUID fromUomId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = (fromUomId != null)
                ? conversionRepository.findByTenantIdAndFromUomIdAndDeletedAtIsNull(tenantId, fromUomId, pageable)
                : conversionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public UoMConversionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public UoMConversionDto create(CreateUoMConversionRequest request) {
        UUID tenantId = tenantContext.current();

        UnitOfMeasure fromUom = uomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromUomId())
                .orElseThrow(() -> AppException.notFound("From UoM not found: " + request.getFromUomId()));
        UnitOfMeasure toUom = uomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToUomId())
                .orElseThrow(() -> AppException.notFound("To UoM not found: " + request.getToUomId()));

        conversionRepository.findByTenantIdAndFromUomIdAndToUomIdAndDeletedAtIsNull(tenantId, request.getFromUomId(), request.getToUomId())
                .ifPresent(e -> { throw AppException.conflict("Conversion already exists between these UoMs"); });

        UoMConversion conversion = buildConversion(tenantId, fromUom, toUom, request.getConversionFactor(), request);
        conversion = conversionRepository.save(conversion);

        if (request.isBidirectional()) {
            boolean reverseExists = conversionRepository.findByTenantIdAndFromUomIdAndToUomIdAndDeletedAtIsNull(
                    tenantId, request.getToUomId(), request.getFromUomId()).isPresent();
            if (!reverseExists) {
                BigDecimal reverseFactor = BigDecimal.ONE.divide(request.getConversionFactor(), MathContext.DECIMAL128);
                UoMConversion reverse = buildConversion(tenantId, toUom, fromUom, reverseFactor, request);
                reverse.setBidirectional(true);
                conversionRepository.save(reverse);
            }
        }

        log.info("UoM conversion created: id={}, from={}, to={}", conversion.getId(), fromUom.getCode(), toUom.getCode());
        return toDto(conversion);
    }

    @Transactional
    public UoMConversionDto update(UUID id, CreateUoMConversionRequest request) {
        UUID tenantId = tenantContext.current();
        UoMConversion conversion = findOrThrow(id);

        UnitOfMeasure fromUom = uomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getFromUomId())
                .orElseThrow(() -> AppException.notFound("From UoM not found: " + request.getFromUomId()));
        UnitOfMeasure toUom = uomRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getToUomId())
                .orElseThrow(() -> AppException.notFound("To UoM not found: " + request.getToUomId()));

        conversion.setFromUomId(fromUom.getId());
        conversion.setFromUomCode(fromUom.getCode());
        conversion.setToUomId(toUom.getId());
        conversion.setToUomCode(toUom.getCode());
        conversion.setConversionFactor(request.getConversionFactor());
        conversion.setBidirectional(request.isBidirectional());
        conversion.setNotes(request.getNotes());

        return toDto(conversionRepository.save(conversion));
    }

    @Transactional
    public void delete(UUID id) {
        UoMConversion conversion = findOrThrow(id);
        conversion.setDeletedAt(LocalDateTime.now());
        conversionRepository.save(conversion);
        log.info("UoM conversion soft-deleted: id={}", id);
    }

    private UoMConversion buildConversion(UUID tenantId, UnitOfMeasure from, UnitOfMeasure to,
                                          BigDecimal factor, CreateUoMConversionRequest request) {
        UoMConversion c = new UoMConversion();
        c.setTenantId(tenantId);
        c.setFromUomId(from.getId());
        c.setFromUomCode(from.getCode());
        c.setToUomId(to.getId());
        c.setToUomCode(to.getCode());
        c.setConversionFactor(factor);
        c.setBidirectional(request.isBidirectional());
        c.setActive(true);
        c.setNotes(request.getNotes());
        return c;
    }

    private UoMConversion findOrThrow(UUID id) {
        return conversionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("UoM conversion not found: " + id));
    }

    private UoMConversionDto toDto(UoMConversion c) {
        UoMConversionDto dto = new UoMConversionDto();
        dto.setId(c.getId());
        dto.setTenantId(c.getTenantId());
        dto.setFromUomId(c.getFromUomId());
        dto.setFromUomCode(c.getFromUomCode());
        dto.setToUomId(c.getToUomId());
        dto.setToUomCode(c.getToUomCode());
        dto.setConversionFactor(c.getConversionFactor());
        dto.setBidirectional(c.isBidirectional());
        dto.setActive(c.isActive());
        dto.setNotes(c.getNotes());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
