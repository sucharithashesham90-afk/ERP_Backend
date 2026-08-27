package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePackagingBagRequest;
import com.erp.platform.modules.agri.dto.PackagingBagDto;
import com.erp.platform.modules.agri.entity.PackagingBag;
import com.erp.platform.modules.agri.repository.PackagingBagRepository;
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
public class PackagingBagService {

    private final PackagingBagRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<PackagingBagDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PackagingBagDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PackagingBagDto create(CreatePackagingBagRequest request) {
        UUID tenantId = tenantContext.current();
        PackagingBag entity = new PackagingBag();
        entity.setTenantId(tenantId);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setPurchaseUnitUomId(request.purchaseUnitUomId());
        entity.setPurchaseUnit(request.purchaseUnit());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("PackagingBag created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PackagingBagDto update(UUID id, CreatePackagingBagRequest request) {
        PackagingBag entity = findOrThrow(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setPurchaseUnitUomId(request.purchaseUnitUomId());
        entity.setPurchaseUnit(request.purchaseUnit());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PackagingBag entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PackagingBag findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PackagingBag not found: " + id));
    }

    private PackagingBagDto toDto(PackagingBag e) {
        return new PackagingBagDto(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getPurchaseUnitUomId(),
                e.getPurchaseUnit(),
                e.isActive()
        );
    }
}
