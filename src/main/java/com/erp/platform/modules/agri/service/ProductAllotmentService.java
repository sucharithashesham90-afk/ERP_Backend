package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProductAllotmentRequest;
import com.erp.platform.modules.agri.dto.ProductAllotmentDto;
import com.erp.platform.modules.agri.entity.Dealer;
import com.erp.platform.modules.agri.entity.PlantVariant;
import com.erp.platform.modules.agri.entity.ProductAllotment;
import com.erp.platform.modules.agri.entity.SalesScheme;
import com.erp.platform.modules.agri.repository.DealerRepository;
import com.erp.platform.modules.agri.repository.PlantVariantRepository;
import com.erp.platform.modules.agri.repository.ProductAllotmentRepository;
import com.erp.platform.modules.agri.repository.SalesSchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
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
public class ProductAllotmentService {

    private final ProductAllotmentRepository repository;
    private final DealerRepository dealerRepository;
    private final PlantVariantRepository variantRepository;
    private final SalesSchemeRepository schemeRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProductAllotmentDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ProductAllotment> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ProductAllotmentDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProductAllotmentDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProductAllotmentDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProductAllotmentDto create(CreateProductAllotmentRequest req) {
        ProductAllotment entity = new ProductAllotment();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("ProductAllotment created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProductAllotmentDto update(UUID id, CreateProductAllotmentRequest req) {
        ProductAllotment entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProductAllotment entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(ProductAllotment entity, CreateProductAllotmentRequest req) {
        entity.setAllotmentNumber(req.getAllotmentNumber());
        entity.setAllotmentDate(req.getAllotmentDate());
        entity.setSeason(req.getSeason());
        entity.setAllottedQuantity(req.getAllottedQuantity());
        entity.setAllottedUom(req.getAllottedUom());
        entity.setRate(req.getRate());
        entity.setSpecialDiscountPercent(req.getSpecialDiscountPercent());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "PENDING");
        entity.setRemarks(req.getRemarks());

        UUID tenantId = tenantContext.current();
        if (req.getDealerId() != null) {
            Dealer dealer = dealerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getDealerId()).orElse(null);
            entity.setDealer(dealer);
        }
        if (req.getPlantVariantId() != null) {
            PlantVariant variant = variantRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantVariantId()).orElse(null);
            entity.setPlantVariant(variant);
        }
        if (req.getSalesSchemeId() != null) {
            SalesScheme scheme = schemeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getSalesSchemeId()).orElse(null);
            entity.setSalesScheme(scheme);
        }
    }

    private ProductAllotment findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Product allotment not found: " + id));
    }

    private ProductAllotmentDto toDto(ProductAllotment e) {
        ProductAllotmentDto dto = new ProductAllotmentDto();
        dto.setId(e.getId());
        dto.setAllotmentNumber(e.getAllotmentNumber());
        dto.setAllotmentDate(e.getAllotmentDate());
        dto.setSeason(e.getSeason());
        dto.setAllottedQuantity(e.getAllottedQuantity());
        dto.setAllottedUom(e.getAllottedUom());
        dto.setRate(e.getRate());
        dto.setSpecialDiscountPercent(e.getSpecialDiscountPercent());
        dto.setStatus(e.getStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getDealer() != null) {
            dto.setDealerId(e.getDealer().getId());
            dto.setDealerName(e.getDealer().getName());
        }
        if (e.getPlantVariant() != null) {
            dto.setPlantVariantId(e.getPlantVariant().getId());
            dto.setPlantVariantName(e.getPlantVariant().getName());
        }
        if (e.getSalesScheme() != null) {
            dto.setSalesSchemeId(e.getSalesScheme().getId());
            dto.setSchemeName(e.getSalesScheme().getSchemeName());
        }
        return dto;
    }
}
