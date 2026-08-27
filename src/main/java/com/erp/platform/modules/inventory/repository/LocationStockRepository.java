package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.LocationStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationStockRepository extends JpaRepository<LocationStock, UUID> {

    Page<LocationStock> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<LocationStock> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<LocationStock> findByTenantIdAndLocationIdAndDeletedAtIsNull(UUID tenantId, UUID locationId, Pageable pageable);

    Page<LocationStock> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);

    Optional<LocationStock> findByTenantIdAndLocationIdAndProductIdAndLotNumberAndDeletedAtIsNull(
            UUID tenantId, UUID locationId, UUID productId, String lotNumber);

    /** Everything currently in one bin — used to check what a putaway would be mixing with. */
    java.util.List<LocationStock> findByTenantIdAndLocationIdAndDeletedAtIsNull(UUID tenantId, UUID locationId);

    /** Where a lot is held, for a pick that starts from a scanned lot rather than a bin. */
    java.util.List<LocationStock> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
