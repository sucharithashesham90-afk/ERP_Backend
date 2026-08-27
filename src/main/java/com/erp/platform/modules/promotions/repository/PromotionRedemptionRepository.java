package com.erp.platform.modules.promotions.repository;

import com.erp.platform.modules.promotions.entity.PromotionRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, UUID> {

    Page<PromotionRedemption> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PromotionRedemption> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByPromotionId(UUID promotionId);
}
