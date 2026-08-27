package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePackingAdviceRequest;
import com.erp.platform.modules.agri.dto.PackingAdviceDto;
import com.erp.platform.modules.agri.entity.PackingAdvice;
import com.erp.platform.modules.agri.repository.PackingAdviceRepository;
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
public class PackingAdviceService {

    private final PackingAdviceRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<PackingAdviceDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PackingAdviceDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PackingAdviceDto create(CreatePackingAdviceRequest request) {
        UUID tenantId = tenantContext.current();
        PackingAdvice entity = new PackingAdvice();
        entity.setTenantId(tenantId);
        entity.setAdviceNumber(request.adviceNumber());
        entity.setAdviceDate(request.adviceDate());
        entity.setProductName(request.productName());
        entity.setPackSize(request.packSize());
        entity.setTotalBags(request.totalBags());
        entity.setLocation(request.location());
        entity.setJobCardNumber(request.jobCardNumber());
        entity.setRemarks(request.remarks());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity = repository.save(entity);
        log.info("PackingAdvice created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PackingAdviceDto update(UUID id, CreatePackingAdviceRequest request) {
        PackingAdvice entity = findOrThrow(id);
        entity.setAdviceNumber(request.adviceNumber());
        entity.setAdviceDate(request.adviceDate());
        entity.setProductName(request.productName());
        entity.setPackSize(request.packSize());
        entity.setTotalBags(request.totalBags());
        entity.setLocation(request.location());
        entity.setJobCardNumber(request.jobCardNumber());
        entity.setRemarks(request.remarks());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PackingAdvice entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PackingAdvice findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PackingAdvice not found: " + id));
    }

    private PackingAdviceDto toDto(PackingAdvice e) {
        return new PackingAdviceDto(
                e.getId(),
                e.getAdviceNumber(),
                e.getAdviceDate(),
                e.getProductName(),
                e.getPackSize(),
                e.getTotalBags(),
                e.getLocation(),
                e.getJobCardNumber(),
                e.getRemarks(),
                e.getStatus()
        );
    }
}
