package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreateLotPurchaseReturnRequest;
import com.erp.platform.modules.purchase.dto.LotPurchaseReturnDto;
import com.erp.platform.modules.purchase.entity.LotPurchaseReturn;
import com.erp.platform.modules.purchase.repository.LotPurchaseReturnRepository;
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
public class LotPurchaseReturnService {

    private final LotPurchaseReturnRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<LotPurchaseReturnDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public LotPurchaseReturnDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LotPurchaseReturnDto create(CreateLotPurchaseReturnRequest request) {
        UUID tenantId = tenantContext.current();
        LotPurchaseReturn entity = new LotPurchaseReturn();
        entity.setTenantId(tenantId);
        entity.setReturnNumber(request.returnNumber());
        entity.setReturnDate(request.returnDate());
        entity.setLotNumber(request.lotNumber());
        entity.setSupplierName(request.supplierName());
        entity.setCropName(request.cropName());
        entity.setReturnQuantityKgs(request.returnQuantityKgs());
        entity.setReturnReason(request.returnReason());
        entity.setOriginalInvoiceNumber(request.originalInvoiceNumber());
        entity.setCreditNoteNumber(request.creditNoteNumber());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity = repository.save(entity);
        log.info("LotPurchaseReturn created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public LotPurchaseReturnDto update(UUID id, CreateLotPurchaseReturnRequest request) {
        LotPurchaseReturn entity = findOrThrow(id);
        entity.setReturnNumber(request.returnNumber());
        entity.setReturnDate(request.returnDate());
        entity.setLotNumber(request.lotNumber());
        entity.setSupplierName(request.supplierName());
        entity.setCropName(request.cropName());
        entity.setReturnQuantityKgs(request.returnQuantityKgs());
        entity.setReturnReason(request.returnReason());
        entity.setOriginalInvoiceNumber(request.originalInvoiceNumber());
        entity.setCreditNoteNumber(request.creditNoteNumber());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        LotPurchaseReturn entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private LotPurchaseReturn findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("LotPurchaseReturn not found: " + id));
    }

    private LotPurchaseReturnDto toDto(LotPurchaseReturn e) {
        return new LotPurchaseReturnDto(
                e.getId(),
                e.getReturnNumber(),
                e.getReturnDate(),
                e.getLotNumber(),
                e.getSupplierName(),
                e.getCropName(),
                e.getReturnQuantityKgs(),
                e.getReturnReason(),
                e.getOriginalInvoiceNumber(),
                e.getCreditNoteNumber(),
                e.getStatus()
        );
    }
}
