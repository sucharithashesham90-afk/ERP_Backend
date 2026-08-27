package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.dto.CreateSalesPlanRequest;
import com.erp.platform.modules.sales.dto.CreateSalesPlanTargetRequest;
import com.erp.platform.modules.sales.dto.SalesPlanDto;
import com.erp.platform.modules.sales.dto.SalesPlanTargetDto;
import com.erp.platform.modules.sales.entity.SalesPlan;
import com.erp.platform.modules.sales.entity.SalesPlan.PlanStatus;
import com.erp.platform.modules.sales.entity.SalesPlanTarget;
import com.erp.platform.modules.sales.repository.SalesPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesPlanService {

    private final SalesPlanRepository salesPlanRepository;
    private final TenantContext tenantContext;

    public PageResponse<SalesPlanDto> list(PlanStatus status, int planYear, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<SalesPlan> page;
        if (status != null && planYear > 0) {
            page = salesPlanRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else if (status != null) {
            page = salesPlanRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else if (planYear > 0) {
            page = salesPlanRepository.findByTenantIdAndPlanYearAndDeletedAtIsNull(tenantId, planYear, pageable);
        } else {
            page = salesPlanRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public SalesPlanDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SalesPlanDto create(CreateSalesPlanRequest request) {
        UUID tenantId = tenantContext.current();
        long count = salesPlanRepository.countByTenantId(tenantId);
        String planNumber = String.format("SP-%d-%03d", Year.now().getValue(), count + 1);

        SalesPlan plan = new SalesPlan();
        plan.setTenantId(tenantId);
        plan.setPlanNumber(planNumber);
        plan.setName(request.getName());
        plan.setPlanType(request.getPlanType());
        plan.setPlanYear(request.getPlanYear());
        plan.setPlanQuarter(request.getPlanQuarter());
        plan.setPlanMonth(request.getPlanMonth());
        plan.setStatus(PlanStatus.DRAFT);
        plan.setTerritory(request.getTerritory());
        plan.setSalesRepId(request.getSalesRepId());
        plan.setSalesRepName(request.getSalesRepName());
        plan.setNotes(request.getNotes());

        if (request.getTargets() != null) {
            BigDecimal totalTarget = BigDecimal.ZERO;
            for (CreateSalesPlanTargetRequest tr : request.getTargets()) {
                SalesPlanTarget target = buildTarget(tenantId, plan, tr);
                plan.getTargets().add(target);
                if (tr.getTargetRevenue() != null) {
                    totalTarget = totalTarget.add(tr.getTargetRevenue());
                }
            }
            plan.setTotalTargetRevenue(totalTarget);
        }

        plan = salesPlanRepository.save(plan);
        log.info("SalesPlan created: id={}, planNumber={}", plan.getId(), plan.getPlanNumber());
        return toDto(plan);
    }

    @Transactional
    public SalesPlanDto update(UUID id, CreateSalesPlanRequest request) {
        UUID tenantId = tenantContext.current();
        SalesPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT sales plans can be updated");
        }

        plan.setName(request.getName());
        plan.setPlanType(request.getPlanType());
        plan.setPlanYear(request.getPlanYear());
        plan.setPlanQuarter(request.getPlanQuarter());
        plan.setPlanMonth(request.getPlanMonth());
        plan.setTerritory(request.getTerritory());
        plan.setSalesRepId(request.getSalesRepId());
        plan.setSalesRepName(request.getSalesRepName());
        plan.setNotes(request.getNotes());

        plan.getTargets().clear();
        if (request.getTargets() != null) {
            BigDecimal totalTarget = BigDecimal.ZERO;
            for (CreateSalesPlanTargetRequest tr : request.getTargets()) {
                SalesPlanTarget target = buildTarget(tenantId, plan, tr);
                plan.getTargets().add(target);
                if (tr.getTargetRevenue() != null) {
                    totalTarget = totalTarget.add(tr.getTargetRevenue());
                }
            }
            plan.setTotalTargetRevenue(totalTarget);
        }

        return toDto(salesPlanRepository.save(plan));
    }

    @Transactional
    public SalesPlanDto approve(UUID id, String approvedBy) {
        SalesPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT plans can be approved");
        }
        plan.setStatus(PlanStatus.APPROVED);
        plan.setApprovedBy(approvedBy);
        plan.setApprovedAt(LocalDateTime.now());
        return toDto(salesPlanRepository.save(plan));
    }

    @Transactional
    public SalesPlanDto activate(UUID id) {
        SalesPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.APPROVED) {
            throw AppException.businessRule("Only APPROVED plans can be activated. Current status: " + plan.getStatus());
        }
        plan.setStatus(PlanStatus.ACTIVE);
        return toDto(salesPlanRepository.save(plan));
    }

    @Transactional
    public SalesPlanDto close(UUID id) {
        SalesPlan plan = findOrThrow(id);
        if (plan.getStatus() == PlanStatus.CLOSED) {
            throw AppException.businessRule("Plan is already CLOSED");
        }
        plan.setStatus(PlanStatus.CLOSED);
        return toDto(salesPlanRepository.save(plan));
    }

    @Transactional
    public void delete(UUID id) {
        SalesPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT plans can be deleted");
        }
        plan.setDeletedAt(LocalDateTime.now());
        salesPlanRepository.save(plan);
        log.info("SalesPlan soft-deleted: id={}", id);
    }

    private SalesPlanTarget buildTarget(UUID tenantId, SalesPlan plan, CreateSalesPlanTargetRequest tr) {
        SalesPlanTarget target = new SalesPlanTarget();
        target.setTenantId(tenantId);
        target.setSalesPlan(plan);
        target.setProductId(tr.getProductId());
        target.setProductName(tr.getProductName());
        target.setCropGroupId(tr.getCropGroupId());
        target.setCropGroupName(tr.getCropGroupName());
        target.setCropId(tr.getCropId());
        target.setCropName(tr.getCropName());
        target.setVarietyId(tr.getVarietyId());
        target.setVarietyName(tr.getVarietyName());
        target.setTargetQuantity(tr.getTargetQuantity() != null ? tr.getTargetQuantity() : BigDecimal.ZERO);
        target.setTargetRevenue(tr.getTargetRevenue() != null ? tr.getTargetRevenue() : BigDecimal.ZERO);
        target.setActualQuantity(BigDecimal.ZERO);
        target.setActualRevenue(BigDecimal.ZERO);
        target.setUnit(tr.getUnit());
        return target;
    }

    private SalesPlan findOrThrow(UUID id) {
        return salesPlanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales plan not found: " + id));
    }

    private SalesPlanDto toDto(SalesPlan p) {
        SalesPlanDto dto = new SalesPlanDto();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setPlanNumber(p.getPlanNumber());
        dto.setName(p.getName());
        dto.setPlanType(p.getPlanType());
        dto.setPlanYear(p.getPlanYear());
        dto.setPlanQuarter(p.getPlanQuarter());
        dto.setPlanMonth(p.getPlanMonth());
        dto.setStatus(p.getStatus());
        dto.setTotalTargetRevenue(p.getTotalTargetRevenue());
        dto.setTotalActualRevenue(p.getTotalActualRevenue());
        dto.setTerritory(p.getTerritory());
        dto.setSalesRepId(p.getSalesRepId());
        dto.setSalesRepName(p.getSalesRepName());
        dto.setApprovedBy(p.getApprovedBy());
        dto.setApprovedAt(p.getApprovedAt());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());
        if (p.getTargets() != null) {
            dto.setTargets(p.getTargets().stream().map(this::toTargetDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private SalesPlanTargetDto toTargetDto(SalesPlanTarget t) {
        SalesPlanTargetDto dto = new SalesPlanTargetDto();
        dto.setId(t.getId());
        dto.setProductId(t.getProductId());
        dto.setProductName(t.getProductName());
        dto.setCropGroupId(t.getCropGroupId());
        dto.setCropGroupName(t.getCropGroupName());
        dto.setCropId(t.getCropId());
        dto.setCropName(t.getCropName());
        dto.setVarietyId(t.getVarietyId());
        dto.setVarietyName(t.getVarietyName());
        dto.setTargetQuantity(t.getTargetQuantity());
        dto.setTargetRevenue(t.getTargetRevenue());
        dto.setActualQuantity(t.getActualQuantity());
        dto.setActualRevenue(t.getActualRevenue());
        dto.setUnit(t.getUnit());
        return dto;
    }
}
