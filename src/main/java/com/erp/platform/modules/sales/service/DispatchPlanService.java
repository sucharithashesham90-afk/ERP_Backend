package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.dto.CreateDispatchPlanRequest;
import com.erp.platform.modules.sales.dto.DispatchPlanDto;
import com.erp.platform.modules.sales.entity.DispatchPlan;
import com.erp.platform.modules.sales.repository.DispatchPlanRepository;
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
public class DispatchPlanService {

    private final DispatchPlanRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<DispatchPlanDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public DispatchPlanDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DispatchPlanDto create(CreateDispatchPlanRequest request) {
        UUID tenantId = tenantContext.current();
        DispatchPlan entity = new DispatchPlan();
        entity.setTenantId(tenantId);
        entity.setPlanNumber(request.planNumber());
        entity.setPlanDate(request.planDate());
        entity.setSalesOrderNumber(request.salesOrderNumber());
        entity.setCustomerName(request.customerName());
        entity.setLocation(request.location());
        entity.setProductName(request.productName());
        entity.setPlannedQtyKgs(request.plannedQtyKgs());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setPlannedDispatchDate(request.plannedDispatchDate());
        entity.setStatus(request.status() != null ? request.status() : "PLANNED");
        entity = repository.save(entity);
        log.info("DispatchPlan created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public DispatchPlanDto update(UUID id, CreateDispatchPlanRequest request) {
        DispatchPlan entity = findOrThrow(id);
        entity.setPlanNumber(request.planNumber());
        entity.setPlanDate(request.planDate());
        entity.setSalesOrderNumber(request.salesOrderNumber());
        entity.setCustomerName(request.customerName());
        entity.setLocation(request.location());
        entity.setProductName(request.productName());
        entity.setPlannedQtyKgs(request.plannedQtyKgs());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setPlannedDispatchDate(request.plannedDispatchDate());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        DispatchPlan entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private DispatchPlan findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("DispatchPlan not found: " + id));
    }

    private DispatchPlanDto toDto(DispatchPlan e) {
        return new DispatchPlanDto(
                e.getId(),
                e.getPlanNumber(),
                e.getPlanDate(),
                e.getSalesOrderNumber(),
                e.getCustomerName(),
                e.getLocation(),
                e.getProductName(),
                e.getPlannedQtyKgs(),
                e.getVehicleNumber(),
                e.getPlannedDispatchDate(),
                e.getStatus()
        );
    }
}
