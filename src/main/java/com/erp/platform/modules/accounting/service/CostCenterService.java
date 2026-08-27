package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.CostCenter;
import com.erp.platform.modules.accounting.entity.CostCenterAllocation;
import com.erp.platform.modules.accounting.repository.CostCenterAllocationRepository;
import com.erp.platform.modules.accounting.repository.CostCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CostCenterService {

    private final CostCenterRepository costCenterRepo;
    private final CostCenterAllocationRepository allocationRepo;
    private final TenantContext tenantContext;

    public PageResponse<CostCenter> list(Pageable pageable) {
        return PageResponse.of(costCenterRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public CostCenter getById(UUID id) {
        return costCenterRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Cost center not found: " + id));
    }

    @Transactional
    public CostCenter create(CostCenter req) {
        UUID tenantId = tenantContext.current();
        if (costCenterRepo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, req.getCode())) {
            throw AppException.conflict("Cost center code already exists: " + req.getCode());
        }
        req.setTenantId(tenantId);
        if (req.getBudgetAmount() == null) req.setBudgetAmount(BigDecimal.ZERO);
        if (req.getActualAmount() == null) req.setActualAmount(BigDecimal.ZERO);
        return costCenterRepo.save(req);
    }

    @Transactional
    public CostCenter update(UUID id, CostCenter req) {
        UUID tenantId = tenantContext.current();
        CostCenter e = costCenterRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Cost center not found: " + id));
        if (!e.getCode().equals(req.getCode()) &&
                costCenterRepo.existsByTenantIdAndCodeAndIdNotAndDeletedAtIsNull(tenantId, req.getCode(), id)) {
            throw AppException.conflict("Cost center code already exists: " + req.getCode());
        }
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setType(req.getType());
        e.setParent(req.getParent());
        e.setBudgetAmount(req.getBudgetAmount() != null ? req.getBudgetAmount() : BigDecimal.ZERO);
        if (req.getActualAmount() != null) {
            e.setActualAmount(req.getActualAmount());
        }
        e.setActive(req.isActive());
        return costCenterRepo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        CostCenter e = costCenterRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Cost center not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        costCenterRepo.save(e);
    }

    public List<CostCenter> getAllActive() {
        return costCenterRepo.findByTenantIdAndActiveAndDeletedAtIsNull(tenantContext.current(), true);
    }

    @Transactional
    public CostCenterAllocation addAllocation(UUID costCenterId, CostCenterAllocation alloc) {
        CostCenter cc = costCenterRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), costCenterId)
                .orElseThrow(() -> AppException.notFound("Cost center not found: " + costCenterId));
        alloc.setCostCenter(cc);
        alloc.setTenantId(tenantContext.current());
        if (alloc.getAmount() == null) alloc.setAmount(BigDecimal.ZERO);
        CostCenterAllocation saved = allocationRepo.save(alloc);
        cc.setActualAmount(cc.getActualAmount().add(alloc.getAmount()));
        costCenterRepo.save(cc);
        return saved;
    }

    public PageResponse<CostCenterAllocation> getAllocations(UUID costCenterId, Pageable pageable) {
        return PageResponse.of(allocationRepo.findByCostCenterIdOrderByAllocationDateDesc(costCenterId, pageable));
    }

    public Map<String, Object> getBudgetSummary(UUID costCenterId) {
        CostCenter cc = getById(costCenterId);
        BigDecimal budget = cc.getBudgetAmount() != null ? cc.getBudgetAmount() : BigDecimal.ZERO;
        BigDecimal actual = cc.getActualAmount() != null ? cc.getActualAmount() : BigDecimal.ZERO;
        BigDecimal remaining = budget.subtract(actual);
        BigDecimal utilizationPercent = BigDecimal.ZERO;
        if (budget.compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercent = actual.multiply(BigDecimal.valueOf(100))
                    .divide(budget, 2, RoundingMode.HALF_UP);
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("budgetAmount", budget);
        summary.put("actualAmount", actual);
        summary.put("remainingAmount", remaining);
        summary.put("utilizationPercent", utilizationPercent);
        return summary;
    }
}
