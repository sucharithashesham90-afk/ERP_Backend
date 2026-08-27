package com.erp.platform.modules.promotions.repository;

import com.erp.platform.modules.promotions.entity.MarketingCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, UUID> {
    Page<MarketingCampaign> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<MarketingCampaign> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
