package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.SeedConversion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeedConversionRepository extends JpaRepository<SeedConversion, UUID> {
    Page<SeedConversion> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    /**
     * Conversions entered on the Seed Conversion screen.
     *
     * <p>Rows written before conversions recorded a source have none, and those were all entered by
     * hand — the automatic ones only started being written when process jobs began stamping them.
     * So a null source counts as manual, and no existing record disappears from the screen.
     */
    @org.springframework.data.jpa.repository.Query(
            "select c from SeedConversion c where c.tenantId = :tenantId and c.deletedAt is null "
          + "and (c.source is null or c.source = 'MANUAL')")
    Page<SeedConversion> findManual(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId,
                                    Pageable pageable);
    Optional<SeedConversion> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
