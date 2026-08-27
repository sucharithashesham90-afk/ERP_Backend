package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.CreateStockReconciliationRequest;
import com.erp.platform.modules.inventory.dto.StockReconciliationDto;
import com.erp.platform.modules.inventory.entity.StockReconciliation;
import com.erp.platform.modules.inventory.repository.StockReconciliationRepository;
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
public class StockReconciliationService {

    private final StockReconciliationRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<StockReconciliationDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public StockReconciliationDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public StockReconciliationDto create(CreateStockReconciliationRequest request) {
        UUID tenantId = tenantContext.current();
        StockReconciliation entity = new StockReconciliation();
        entity.setTenantId(tenantId);
        entity.setReconNumber(request.reconNumber());
        entity.setReconDate(request.reconDate());
        entity.setLocation(request.location());
        entity.setMaterialCode(request.materialCode());
        entity.setMaterialName(request.materialName());
        entity.setBookQty(request.bookQty());
        entity.setPhysicalQty(request.physicalQty());
        entity.setDifference(request.difference());
        entity.setUnit(request.unit());
        entity.setApprovedBy(request.approvedBy());
        entity.setRemarks(request.remarks());
        entity.setStatus(request.status() != null ? request.status() : "DRAFT");
        entity = repository.save(entity);
        log.info("StockReconciliation created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public StockReconciliationDto update(UUID id, CreateStockReconciliationRequest request) {
        StockReconciliation entity = findOrThrow(id);
        entity.setReconNumber(request.reconNumber());
        entity.setReconDate(request.reconDate());
        entity.setLocation(request.location());
        entity.setMaterialCode(request.materialCode());
        entity.setMaterialName(request.materialName());
        entity.setBookQty(request.bookQty());
        entity.setPhysicalQty(request.physicalQty());
        entity.setDifference(request.difference());
        entity.setUnit(request.unit());
        entity.setApprovedBy(request.approvedBy());
        entity.setRemarks(request.remarks());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        StockReconciliation entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private StockReconciliation findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("StockReconciliation not found: " + id));
    }

    private StockReconciliationDto toDto(StockReconciliation e) {
        return new StockReconciliationDto(
                e.getId(),
                e.getReconNumber(),
                e.getReconDate(),
                e.getLocation(),
                e.getMaterialCode(),
                e.getMaterialName(),
                e.getBookQty(),
                e.getPhysicalQty(),
                e.getDifference(),
                e.getUnit(),
                e.getApprovedBy(),
                e.getRemarks(),
                e.getStatus()
        );
    }
}
