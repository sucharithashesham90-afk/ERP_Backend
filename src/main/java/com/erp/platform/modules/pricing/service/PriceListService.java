package com.erp.platform.modules.pricing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.pricing.entity.PriceList;
import com.erp.platform.modules.pricing.entity.PriceListItem;
import com.erp.platform.modules.pricing.repository.PriceListItemRepository;
import com.erp.platform.modules.pricing.repository.PriceListRepository;
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
public class PriceListService {

    private final PriceListRepository repo;
    private final PriceListItemRepository itemRepo;
    private final TenantContext tenantContext;

    public PageResponse<PriceList> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public PriceList getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public PriceList create(PriceList req) {
        req.setTenantId(tenantContext.current());
        return repo.save(req);
    }

    @Transactional
    public PriceList update(UUID id, PriceList req) {
        PriceList e = findOrThrow(id);
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setCurrency(req.getCurrency());
        e.setDefault(req.isDefault());
        e.setActive(req.isActive());
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        PriceList e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    @Transactional
    public PriceListItem addItem(UUID priceListId, PriceListItem item) {
        PriceList priceList = findOrThrow(priceListId);
        item.setPriceList(priceList);
        item.setTenantId(tenantContext.current());
        return itemRepo.save(item);
    }

    @Transactional
    public void removeItem(UUID itemId) {
        PriceListItem item = itemRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), itemId)
                .orElseThrow(() -> AppException.notFound("Price list item not found: " + itemId));
        item.setDeletedAt(LocalDateTime.now());
        itemRepo.save(item);
    }

    public List<PriceListItem> getItemsForPriceList(UUID priceListId) {
        findOrThrow(priceListId);
        return itemRepo.findByPriceListIdAndDeletedAtIsNull(priceListId);
    }

    private PriceList findOrThrow(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Price list not found: " + id));
    }
}
