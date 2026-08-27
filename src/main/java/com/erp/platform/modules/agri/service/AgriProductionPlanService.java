package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProductionPlanRequest;
import com.erp.platform.modules.agri.dto.ProductionPlanDto;
import com.erp.platform.modules.agri.entity.AgriProductionPlan;
import com.erp.platform.modules.agri.repository.AgriProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AgriProductionPlanService {

    private final AgriProductionPlanRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionPlanDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<AgriProductionPlan> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ProductionPlanDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProductionPlanDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProductionPlanDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionPlanDto create(CreateProductionPlanRequest req) {
        UUID tenantId = tenantContext.current();
        AgriProductionPlan entity = new AgriProductionPlan();
        entity.setTenantId(tenantId);
        applyRequest(entity, req);
        entity.setPlanNumber(generatePlanNumber(tenantId));
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");
        entity = repository.save(entity);
        log.info("AgriProductionPlan created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProductionPlanDto update(UUID id, CreateProductionPlanRequest req) {
        AgriProductionPlan entity = findOrThrow(id);
        applyRequest(entity, req);
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        AgriProductionPlan entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private String generatePlanNumber(UUID tenantId) {
        long count = repository.countByTenantIdAndDeletedAtIsNull(tenantId);
        return String.format("PP-%d-%04d", Year.now().getValue(), count + 1);
    }

    private void applyRequest(AgriProductionPlan e, CreateProductionPlanRequest r) {
        e.setPlanName(r.getPlanName());
        e.setSeedCategoryId(r.getSeedCategoryId());
        e.setSeedCategoryName(r.getSeedCategoryName());
        e.setSeasonPeriodId(r.getSeasonPeriodId());
        e.setSeasonPeriodName(r.getSeasonPeriodName());
        e.setRemarks(r.getRemarks());
    }

    private AgriProductionPlan findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production plan not found: " + id));
    }

    private ProductionPlanDto toDto(AgriProductionPlan e) {
        ProductionPlanDto dto = new ProductionPlanDto();
        dto.setId(e.getId());
        dto.setPlanNumber(e.getPlanNumber());
        dto.setPlanName(e.getPlanName());
        dto.setSeedCategoryId(e.getSeedCategoryId());
        dto.setSeedCategoryName(e.getSeedCategoryName());
        dto.setSeasonPeriodId(e.getSeasonPeriodId());
        dto.setSeasonPeriodName(e.getSeasonPeriodName());
        dto.setStatus(e.getStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
