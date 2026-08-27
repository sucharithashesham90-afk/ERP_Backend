package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateSalesSchemeRequest;
import com.erp.platform.modules.agri.dto.SalesSchemeDto;
import com.erp.platform.modules.agri.entity.SalesScheme;
import com.erp.platform.modules.agri.repository.SalesSchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesSchemeService {

    private final SalesSchemeRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<SalesSchemeDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<SalesScheme> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<SalesSchemeDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<SalesSchemeDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public SalesSchemeDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SalesSchemeDto create(CreateSalesSchemeRequest req) {
        SalesScheme entity = new SalesScheme();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("SalesScheme created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public SalesSchemeDto update(UUID id, CreateSalesSchemeRequest req) {
        SalesScheme entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        SalesScheme entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(SalesScheme entity, CreateSalesSchemeRequest req) {
        entity.setSchemeName(req.getSchemeName());
        entity.setSchemeCode(req.getSchemeCode());
        entity.setSeason(req.getSeason());
        entity.setValidFrom(req.getValidFrom());
        entity.setValidTo(req.getValidTo());
        entity.setSchemeType(req.getSchemeType());
        entity.setDiscountPercent(req.getDiscountPercent());
        entity.setFlatDiscountAmount(req.getFlatDiscountAmount());
        entity.setRemarks(req.getRemarks());
        entity.setActive(req.isActive());
    }

    private SalesScheme findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales scheme not found: " + id));
    }

    private SalesSchemeDto toDto(SalesScheme e) {
        SalesSchemeDto dto = new SalesSchemeDto();
        dto.setId(e.getId());
        dto.setSchemeName(e.getSchemeName());
        dto.setSchemeCode(e.getSchemeCode());
        dto.setSeason(e.getSeason());
        dto.setValidFrom(e.getValidFrom());
        dto.setValidTo(e.getValidTo());
        dto.setSchemeType(e.getSchemeType());
        dto.setDiscountPercent(e.getDiscountPercent());
        dto.setFlatDiscountAmount(e.getFlatDiscountAmount());
        dto.setRemarks(e.getRemarks());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
