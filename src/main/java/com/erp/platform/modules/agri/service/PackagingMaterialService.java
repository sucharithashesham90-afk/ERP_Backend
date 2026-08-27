package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePackagingMaterialRequest;
import com.erp.platform.modules.agri.dto.PackagingMaterialDto;
import com.erp.platform.modules.agri.entity.PackagingMaterial;
import com.erp.platform.modules.agri.repository.PackagingMaterialRepository;
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
public class PackagingMaterialService {

    private final PackagingMaterialRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<PackagingMaterialDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PackagingMaterialDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PackagingMaterialDto create(CreatePackagingMaterialRequest request) {
        UUID tenantId = tenantContext.current();
        PackagingMaterial entity = new PackagingMaterial();
        entity.setTenantId(tenantId);
        entity.setMaterialCode(request.materialCode());
        entity.setMaterialName(request.materialName());
        entity.setDescription(request.description());
        entity.setMaterialType(request.materialType());
        entity.setClassOfSeed(request.classOfSeed());
        entity.setHybrid(request.hybrid());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("PackagingMaterial created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PackagingMaterialDto update(UUID id, CreatePackagingMaterialRequest request) {
        PackagingMaterial entity = findOrThrow(id);
        entity.setMaterialCode(request.materialCode());
        entity.setMaterialName(request.materialName());
        entity.setDescription(request.description());
        entity.setMaterialType(request.materialType());
        entity.setClassOfSeed(request.classOfSeed());
        entity.setHybrid(request.hybrid());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PackagingMaterial entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PackagingMaterial findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PackagingMaterial not found: " + id));
    }

    private PackagingMaterialDto toDto(PackagingMaterial e) {
        return new PackagingMaterialDto(
                e.getId(),
                e.getMaterialCode(),
                e.getMaterialName(),
                e.getDescription(),
                e.getMaterialType(),
                e.getClassOfSeed(),
                e.isHybrid(),
                e.isActive()
        );
    }
}
