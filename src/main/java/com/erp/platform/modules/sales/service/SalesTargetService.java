package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesTarget;
import com.erp.platform.modules.sales.entity.SalesTarget.TargetStatus;
import com.erp.platform.modules.sales.repository.SalesTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesTargetService {

    private final SalesTargetRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<SalesTarget> list(TargetStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (status != null) {
            return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNullAndStatus(tenantId, status, pageable));
        }
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public SalesTarget getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public SalesTarget create(SalesTarget req) {
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public SalesTarget update(UUID id, SalesTarget req) {
        SalesTarget e = findOrThrow(id);
        e.setName(req.getName());
        e.setPeriod(req.getPeriod());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setTerritory(req.getTerritory());
        e.setSalesRep(req.getSalesRep());
        e.setTargetAmount(req.getTargetAmount());
        e.setTargetQuantity(req.getTargetQuantity());
        e.setStatus(req.getStatus());
        e.setNotes(req.getNotes());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        SalesTarget e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    @Transactional
    public SalesTarget updateActuals(UUID id, BigDecimal actualAmount, BigDecimal actualQuantity) {
        SalesTarget e = findOrThrow(id);
        e.setActualAmount(actualAmount != null ? actualAmount : BigDecimal.ZERO);
        e.setActualQuantity(actualQuantity != null ? actualQuantity : BigDecimal.ZERO);
        // Auto-update status if target is achieved
        if (e.getTargetAmount() != null && e.getActualAmount().compareTo(e.getTargetAmount()) >= 0) {
            e.setStatus(TargetStatus.ACHIEVED);
        }
        return repo.save(e);
    }

    private SalesTarget findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales target not found: " + id));
    }
}
