package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.ScrapDisposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScrapDisposalRepository extends JpaRepository<ScrapDisposal, UUID> {

    Page<ScrapDisposal> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ScrapDisposal> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<ScrapDisposal> findByTenantIdAndScrapEntry_IdAndDeletedAtIsNull(UUID tenantId, UUID scrapEntryId);
}
