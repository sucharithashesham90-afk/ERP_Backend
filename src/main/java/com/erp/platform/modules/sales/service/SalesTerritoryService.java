package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesRepresentative;
import com.erp.platform.modules.sales.entity.SalesTerritory;
import com.erp.platform.modules.sales.repository.SalesRepresentativeRepository;
import com.erp.platform.modules.sales.repository.SalesTerritoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class SalesTerritoryService {

    private final SalesTerritoryRepository repo;
    private final SalesRepresentativeRepository repRepo;
    private final TenantContext tenantContext;

    public PageResponse<SalesTerritory> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public SalesTerritory getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public SalesTerritory create(SalesTerritory req) {
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public SalesTerritory update(UUID id, SalesTerritory req) {
        SalesTerritory e = findOrThrow(id);
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setRegion(req.getRegion());
        e.setState(req.getState());
        e.setCountry(req.getCountry());
        e.setAssignedToEmployeeId(req.getAssignedToEmployeeId());
        e.setAssignedToEmployeeName(req.getAssignedToEmployeeName());
        e.setLocation(req.getLocation());
        e.setCurrencyCode(req.getCurrencyCode());
        e.setExport(req.isExport());
        e.setActive(req.isActive());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        SalesTerritory e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    public List<SalesRepresentative> getRepsForTerritory(UUID territoryId) {
        findOrThrow(territoryId);
        return repRepo.findByTenantIdAndTerritoryIdAndDeletedAtIsNull(tenantContext.current(), territoryId);
    }

    private SalesTerritory findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales territory not found: " + id));
    }
}
