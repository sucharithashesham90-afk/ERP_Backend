package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProductionAreaRequest;
import com.erp.platform.modules.agri.dto.ProductionAreaDto;
import com.erp.platform.modules.agri.entity.ProductionArea;
import com.erp.platform.modules.agri.repository.ProductionAreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductionAreaService {

    private final ProductionAreaRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProductionAreaDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public List<ProductionAreaDto> findAllActive() {
        return repository.findByTenantIdAndDeletedAtIsNullAndActiveTrue(tenantContext.current())
                .stream().map(this::toDto).toList();
    }

    public ProductionAreaDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductionAreaDto create(CreateProductionAreaRequest req) {
        UUID tenantId = tenantContext.current();
        ProductionArea entity = new ProductionArea();
        entity.setTenantId(tenantId);
        entity.setCode(req.code());
        entity.setName(req.name());
        entity.setDescription(req.description());
        entity.setVillage(req.village());
        entity.setDistrict(req.district());
        entity.setState(req.state());
        entity.setTotalAreaAcres(req.totalAreaAcres());
        entity.setActive(req.active());
        entity = repository.save(entity);
        log.info("ProductionArea created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProductionAreaDto update(UUID id, CreateProductionAreaRequest req) {
        ProductionArea entity = findOrThrow(id);
        entity.setCode(req.code());
        entity.setName(req.name());
        entity.setDescription(req.description());
        entity.setVillage(req.village());
        entity.setDistrict(req.district());
        entity.setState(req.state());
        entity.setTotalAreaAcres(req.totalAreaAcres());
        entity.setActive(req.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProductionArea entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProductionArea findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProductionArea not found: " + id));
    }

    private ProductionAreaDto toDto(ProductionArea e) {
        return new ProductionAreaDto(
                e.getId(), e.getCode(), e.getName(), e.getDescription(),
                e.getVillage(), e.getDistrict(), e.getState(),
                e.getTotalAreaAcres(), e.isActive()
        );
    }
}
