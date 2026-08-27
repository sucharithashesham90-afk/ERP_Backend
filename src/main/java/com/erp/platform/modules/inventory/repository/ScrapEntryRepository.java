package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.ScrapEntry;
import com.erp.platform.modules.inventory.entity.ScrapEntry.ScrapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScrapEntryRepository extends JpaRepository<ScrapEntry, UUID> {

    Page<ScrapEntry> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ScrapEntry> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    Page<ScrapEntry> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ScrapStatus status, Pageable pageable);

    Page<ScrapEntry> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);
}
