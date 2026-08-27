package com.erp.platform.modules.promotions.repository;

import com.erp.platform.modules.promotions.entity.PromotionProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, UUID> {

    Page<PromotionProduct> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PromotionProduct> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<PromotionProduct> findByPromotionId(UUID promotionId);
}
