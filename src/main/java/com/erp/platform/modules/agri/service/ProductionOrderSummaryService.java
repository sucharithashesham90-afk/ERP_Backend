package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProductionOrderSummaryRequest;
import com.erp.platform.modules.agri.dto.ProductionOrderSummaryDto;
import com.erp.platform.modules.agri.entity.ProductionOrderSummary;
import com.erp.platform.modules.agri.repository.ProductionOrderSummaryRepository;
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
public class ProductionOrderSummaryService {

    private final ProductionOrderSummaryRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionOrderSummaryDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProductionOrderSummaryDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionOrderSummaryDto create(CreateProductionOrderSummaryRequest request) {
        UUID tenantId = tenantContext.current();
        ProductionOrderSummary entity = new ProductionOrderSummary();
        entity.setTenantId(tenantId);
        entity.setOrderNumber(request.orderNumber());
        entity.setSeason(request.season());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setPlannedQtyKgs(request.plannedQtyKgs());
        entity.setActualQtyKgs(request.actualQtyKgs());
        entity.setLocation(request.location());
        entity.setProcessingStatus(request.processingStatus() != null ? request.processingStatus() : "PENDING");
        entity.setRemarks(request.remarks());
        entity = repository.save(entity);
        log.info("ProductionOrderSummary created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProductionOrderSummaryDto update(UUID id, CreateProductionOrderSummaryRequest request) {
        ProductionOrderSummary entity = findOrThrow(id);
        entity.setOrderNumber(request.orderNumber());
        entity.setSeason(request.season());
        entity.setCropName(request.cropName());
        entity.setVarietyName(request.varietyName());
        entity.setPlannedQtyKgs(request.plannedQtyKgs());
        entity.setActualQtyKgs(request.actualQtyKgs());
        entity.setLocation(request.location());
        entity.setProcessingStatus(request.processingStatus());
        entity.setRemarks(request.remarks());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProductionOrderSummary entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProductionOrderSummary findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProductionOrderSummary not found: " + id));
    }

    private ProductionOrderSummaryDto toDto(ProductionOrderSummary e) {
        return new ProductionOrderSummaryDto(
                e.getId(),
                e.getOrderNumber(),
                e.getSeason(),
                e.getCropName(),
                e.getVarietyName(),
                e.getPlannedQtyKgs(),
                e.getActualQtyKgs(),
                e.getLocation(),
                e.getProcessingStatus(),
                e.getRemarks()
        );
    }
}
