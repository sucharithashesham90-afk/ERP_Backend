package com.erp.platform.modules.planning.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.planning.dto.CreateProductionPlanRequest;
import com.erp.platform.modules.planning.dto.ProductionPlanDto;
import com.erp.platform.modules.planning.entity.ProductionPlan;
import com.erp.platform.modules.planning.entity.ProductionPlan.PlanStatus;
import com.erp.platform.modules.planning.entity.ProductionPlanItem;
import com.erp.platform.modules.planning.repository.ProductionPlanRepository;
import com.erp.platform.modules.sales.repository.SalesPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final SalesPlanRepository salesPlanRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionPlanDto> list(PlanStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? productionPlanRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : productionPlanRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public ProductionPlanDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionPlanDto create(CreateProductionPlanRequest request) {
        UUID tenantId = tenantContext.current();

        ProductionPlan plan = new ProductionPlan();
        plan.setTenantId(tenantId);
        plan.setPlanNumber(generatePlanNumber(tenantId));
        plan.setName(request.getName());
        plan.setPlanType(request.getPlanType());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(PlanStatus.DRAFT);
        plan.setTotalProducedQty(BigDecimal.ZERO);
        plan.setNotes(request.getNotes());
        plan.setSeedCategoryId(request.getSeedCategoryId());
        plan.setSeedCategoryName(request.getSeedCategoryName());
        plan.setSeasonPeriodId(request.getSeasonPeriodId());
        plan.setSeasonPeriodName(request.getSeasonPeriodName());
        if (request.getSalesPlanId() != null) {
            plan.setSalesPlanId(request.getSalesPlanId());
            final ProductionPlan planRef = plan;
            salesPlanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getSalesPlanId())
                    .ifPresent(sp -> planRef.setSalesPlanName(sp.getName()));
        }

        List<ProductionPlanItem> items = buildItems(plan, tenantId, request.getItems());
        plan.setItems(items);

        BigDecimal totalTarget = items.stream()
                .map(ProductionPlanItem::getTargetQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        plan.setTotalTargetQty(totalTarget);

        plan = productionPlanRepository.save(plan);
        log.info("ProductionPlan created: id={}, number={}", plan.getId(), plan.getPlanNumber());
        return toDto(plan);
    }

    @Transactional
    public ProductionPlanDto update(UUID id, CreateProductionPlanRequest request) {
        ProductionPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT plans can be updated");
        }

        plan.setName(request.getName());
        plan.setPlanType(request.getPlanType());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setNotes(request.getNotes());
        plan.setSeedCategoryId(request.getSeedCategoryId());
        plan.setSeedCategoryName(request.getSeedCategoryName());
        plan.setSeasonPeriodId(request.getSeasonPeriodId());
        plan.setSeasonPeriodName(request.getSeasonPeriodName());
        plan.setSalesPlanId(request.getSalesPlanId());
        if (request.getSalesPlanId() != null) {
            final ProductionPlan planRef = plan;
            salesPlanRepository.findByTenantIdAndIdAndDeletedAtIsNull(plan.getTenantId(), request.getSalesPlanId())
                    .ifPresent(sp -> planRef.setSalesPlanName(sp.getName()));
        } else {
            plan.setSalesPlanName(null);
        }

        plan.getItems().clear();
        List<ProductionPlanItem> items = buildItems(plan, plan.getTenantId(), request.getItems());
        plan.getItems().addAll(items);

        BigDecimal totalTarget = items.stream()
                .map(ProductionPlanItem::getTargetQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        plan.setTotalTargetQty(totalTarget);

        plan = productionPlanRepository.save(plan);
        log.info("ProductionPlan updated: id={}", plan.getId());
        return toDto(plan);
    }

    @Transactional
    public ProductionPlanDto updateStatus(UUID id, PlanStatus status) {
        ProductionPlan plan = findOrThrow(id);
        plan.setStatus(status);
        return toDto(productionPlanRepository.save(plan));
    }

    @Transactional
    public void delete(UUID id) {
        ProductionPlan plan = findOrThrow(id);
        if (plan.getStatus() != PlanStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT plans can be deleted");
        }
        plan.setDeletedAt(LocalDateTime.now());
        productionPlanRepository.save(plan);
        log.info("ProductionPlan soft-deleted: id={}", id);
    }

    private List<ProductionPlanItem> buildItems(ProductionPlan plan, UUID tenantId,
            List<CreateProductionPlanRequest.ItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            ProductionPlanItem item = new ProductionPlanItem();
            item.setTenantId(tenantId);
            item.setPlan(plan);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setTargetQuantity(r.getTargetQuantity() != null ? r.getTargetQuantity() : BigDecimal.ZERO);
            item.setProducedQuantity(BigDecimal.ZERO);
            item.setUnit(r.getUnit());
            item.setPriorityOrder(r.getPriorityOrder());
            item.setNotes(r.getNotes());
            return item;
        }).collect(Collectors.toList());
    }

    private ProductionPlanDto toDto(ProductionPlan plan) {
        ProductionPlanDto dto = new ProductionPlanDto();
        dto.setId(plan.getId());
        dto.setTenantId(plan.getTenantId());
        dto.setPlanNumber(plan.getPlanNumber());
        dto.setName(plan.getName());
        dto.setPlanType(plan.getPlanType());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setStatus(plan.getStatus());
        dto.setTotalTargetQty(plan.getTotalTargetQty());
        dto.setTotalProducedQty(plan.getTotalProducedQty());
        dto.setNotes(plan.getNotes());
        dto.setSalesPlanId(plan.getSalesPlanId());
        dto.setSalesPlanName(plan.getSalesPlanName());
        dto.setSeedCategoryId(plan.getSeedCategoryId());
        dto.setSeedCategoryName(plan.getSeedCategoryName());
        dto.setSeasonPeriodId(plan.getSeasonPeriodId());
        dto.setSeasonPeriodName(plan.getSeasonPeriodName());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());

        if (plan.getItems() != null) {
            dto.setItems(plan.getItems().stream().map(item -> {
                ProductionPlanDto.ItemDto idto = new ProductionPlanDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setTargetQuantity(item.getTargetQuantity());
                idto.setProducedQuantity(item.getProducedQuantity());
                idto.setUnit(item.getUnit());
                idto.setPriorityOrder(item.getPriorityOrder());
                idto.setNotes(item.getNotes());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private ProductionPlan findOrThrow(UUID id) {
        return productionPlanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Production plan not found: " + id));
    }

    private String generatePlanNumber(UUID tenantId) {
        long count = productionPlanRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("PP-%s-%03d", year, count + 1);
    }
}
