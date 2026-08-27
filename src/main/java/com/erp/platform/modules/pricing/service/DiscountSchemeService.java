package com.erp.platform.modules.pricing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.pricing.entity.DiscountScheme;
import com.erp.platform.modules.pricing.entity.DiscountSlab;
import com.erp.platform.modules.pricing.repository.DiscountSchemeRepository;
import com.erp.platform.modules.pricing.repository.DiscountSlabRepository;
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
public class DiscountSchemeService {

    private final DiscountSchemeRepository repo;
    private final DiscountSlabRepository slabRepo;
    private final TenantContext tenantContext;

    public PageResponse<DiscountScheme> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public DiscountScheme getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public DiscountScheme create(DiscountScheme req) {
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public DiscountScheme update(UUID id, DiscountScheme req) {
        DiscountScheme e = findOrThrow(id);
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setSchemeType(req.getSchemeType());
        e.setDiscountPercent(req.getDiscountPercent());
        e.setCustomerType(req.getCustomerType());
        e.setActive(req.isActive());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        DiscountScheme e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    @Transactional
    public DiscountSlab addSlab(UUID schemeId, DiscountSlab slab) {
        DiscountScheme scheme = findOrThrow(schemeId);
        slab.setScheme(scheme);
        slab.setTenantId(tenantContext.current());
        return slabRepo.save(slab);
    }

    @Transactional
    public void removeSlab(UUID slabId) {
        DiscountSlab slab = slabRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), slabId)
                .orElseThrow(() -> AppException.notFound("Discount slab not found: " + slabId));
        slab.setDeletedAt(LocalDateTime.now());
        slabRepo.save(slab);
    }

    public List<DiscountSlab> getSlabs(UUID schemeId) {
        findOrThrow(schemeId);
        return slabRepo.findBySchemeIdOrderByMinValue(schemeId);
    }

    private DiscountScheme findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Discount scheme not found: " + id));
    }
}
