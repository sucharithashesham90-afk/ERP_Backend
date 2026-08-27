package com.erp.platform.modules.pricing.repository;

import com.erp.platform.modules.pricing.entity.PriceListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, UUID> {

    List<PriceListItem> findByPriceListIdAndDeletedAtIsNull(UUID priceListId);

    Page<PriceListItem> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PriceListItem> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @org.springframework.data.jpa.repository.Query(
        "SELECT i FROM PriceListItem i WHERE i.priceList.id = :priceListId AND i.product.id = :productId " +
        "AND i.deletedAt IS NULL ORDER BY i.minQuantity ASC")
    List<PriceListItem> findByPriceListIdAndProductId(UUID priceListId, UUID productId);
}
