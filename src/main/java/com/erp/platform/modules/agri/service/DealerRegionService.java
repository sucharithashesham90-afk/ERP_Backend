package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateDealerRegionRequest;
import com.erp.platform.modules.agri.dto.DealerRegionDto;
import com.erp.platform.modules.agri.entity.DealerRegion;
import com.erp.platform.modules.agri.repository.DealerRegionRepository;
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
public class DealerRegionService {

    private final DealerRegionRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<DealerRegionDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<DealerRegion> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<DealerRegionDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<DealerRegionDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public DealerRegionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DealerRegionDto create(CreateDealerRegionRequest req) {
        DealerRegion entity = new DealerRegion();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("DealerRegion created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public DealerRegionDto update(UUID id, CreateDealerRegionRequest req) {
        DealerRegion entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        DealerRegion entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(DealerRegion entity, CreateDealerRegionRequest req) {
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setStatesCovered(req.getStatesCovered());
        entity.setActiveSeason(req.getActiveSeason());
        entity.setActive(req.isActive());
    }

    private DealerRegion findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Dealer region not found: " + id));
    }

    private DealerRegionDto toDto(DealerRegion e) {
        DealerRegionDto dto = new DealerRegionDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setDescription(e.getDescription());
        dto.setStatesCovered(e.getStatesCovered());
        dto.setActiveSeason(e.getActiveSeason());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
