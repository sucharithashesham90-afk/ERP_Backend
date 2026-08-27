package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeasonPeriodRequest;
import com.erp.platform.modules.agri.dto.SeasonPeriodDto;
import com.erp.platform.modules.agri.entity.SeasonPeriod;
import com.erp.platform.modules.agri.repository.SeasonPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeasonPeriodService {

    private final SeasonPeriodRepository seasonPeriodRepository;
    private final TenantContext tenantContext;

    public PageResponse<SeasonPeriodDto> list(Pageable pageable) {
        return PageResponse.of(seasonPeriodRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public PageResponse<SeasonPeriodDto> list(String periodType, Pageable pageable) {
        if (periodType == null || periodType.isBlank()) return list(pageable);
        return PageResponse.of(seasonPeriodRepository
                .findByTenantIdAndPeriodTypeAndDeletedAtIsNull(tenantContext.current(), periodType, pageable)
                .map(this::toDto));
    }

    public SeasonPeriodDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        SeasonPeriod entity = seasonPeriodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeasonPeriod not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public SeasonPeriodDto create(CreateSeasonPeriodRequest request) {
        SeasonPeriod entity = new SeasonPeriod();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        return toDto(seasonPeriodRepository.save(entity));
    }

    @Transactional
    public SeasonPeriodDto update(UUID id, CreateSeasonPeriodRequest request) {
        UUID tenantId = tenantContext.current();
        SeasonPeriod entity = seasonPeriodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeasonPeriod not found: " + id));
        applyRequest(entity, request);
        return toDto(seasonPeriodRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        SeasonPeriod entity = seasonPeriodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("SeasonPeriod not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        seasonPeriodRepository.save(entity);
    }

    private void applyRequest(SeasonPeriod e, CreateSeasonPeriodRequest r) {
        e.setSeasonId(r.getSeasonId());
        e.setSeasonName(r.getSeasonName());
        e.setPeriodName(r.getPeriodName());
        e.setFromDate(r.getFromDate());
        e.setToDate(r.getToDate());
        e.setDescription(r.getDescription());
        e.setActive(r.isActive());
        if (r.getPeriodType() != null) e.setPeriodType(r.getPeriodType());
    }

    private SeasonPeriodDto toDto(SeasonPeriod e) {
        SeasonPeriodDto dto = new SeasonPeriodDto();
        dto.setId(e.getId());
        dto.setSeasonId(e.getSeasonId());
        dto.setSeasonName(e.getSeasonName());
        dto.setPeriodName(e.getPeriodName());
        dto.setFromDate(e.getFromDate());
        dto.setToDate(e.getToDate());
        dto.setDescription(e.getDescription());
        dto.setActive(e.isActive());
        dto.setPeriodType(e.getPeriodType());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
