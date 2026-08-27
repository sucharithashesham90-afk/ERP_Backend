package com.erp.platform.modules.quality.service;
import java.util.UUID;

import com.erp.platform.modules.quality.dto.CreateQualityStandardRequest;
import com.erp.platform.modules.quality.dto.QualityStandardDto;
import com.erp.platform.modules.quality.entity.QualityStandard;
import com.erp.platform.modules.quality.repository.QualityStandardRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QualityStandardService {

    private final QualityStandardRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<QualityStandardDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<QualityStandard> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public QualityStandardDto create(CreateQualityStandardRequest req) {
        QualityStandard e = new QualityStandard();
        e.setTenantId(tenantContext.current());
        e.setStandardCode(req.standardCode());
        e.setStandardName(req.standardName());
        e.setCropName(req.cropName());
        e.setSeedClass(req.seedClass());
        e.setMinGerminationPercent(req.minGerminationPercent());
        e.setMinPurityPercent(req.minPurityPercent());
        e.setMaxMoisturePercent(req.maxMoisturePercent());
        e.setMaxInertMatterPercent(req.maxInertMatterPercent());
        e.setDescription(req.description());
        e.setActive(req.active() != null ? req.active() : true);
        return toDto(repository.save(e));
    }

    public QualityStandardDto update(UUID id, CreateQualityStandardRequest req) {
        QualityStandard e = repository.findById(id).orElseThrow();
        e.setStandardCode(req.standardCode());
        e.setStandardName(req.standardName());
        e.setCropName(req.cropName());
        e.setSeedClass(req.seedClass());
        e.setMinGerminationPercent(req.minGerminationPercent());
        e.setMinPurityPercent(req.minPurityPercent());
        e.setMaxMoisturePercent(req.maxMoisturePercent());
        e.setMaxInertMatterPercent(req.maxInertMatterPercent());
        e.setDescription(req.description());
        e.setActive(req.active());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        QualityStandard e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private QualityStandardDto toDto(QualityStandard e) {
        return new QualityStandardDto(
                e.getId(), e.getStandardCode(), e.getStandardName(),
                e.getCropName(), e.getSeedClass(), e.getMinGerminationPercent(),
                e.getMinPurityPercent(), e.getMaxMoisturePercent(),
                e.getMaxInertMatterPercent(), e.getDescription(), e.getActive());
    }
}

