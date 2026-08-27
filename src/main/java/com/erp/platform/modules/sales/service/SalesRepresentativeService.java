package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesRepresentative;
import com.erp.platform.modules.sales.repository.SalesRepresentativeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesRepresentativeService {

    private final SalesRepresentativeRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<SalesRepresentative> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public SalesRepresentative getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public SalesRepresentative create(SalesRepresentative req) {
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public SalesRepresentative update(UUID id, SalesRepresentative req) {
        SalesRepresentative e = findOrThrow(id);
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setEmail(req.getEmail());
        e.setPhone(req.getPhone());
        e.setTerritory(req.getTerritory());
        e.setTargetAmount(req.getTargetAmount());
        e.setCommissionPercent(req.getCommissionPercent());
        e.setActive(req.isActive());
        e.setNotes(req.getNotes());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        SalesRepresentative e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    private SalesRepresentative findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales representative not found: " + id));
    }
}
