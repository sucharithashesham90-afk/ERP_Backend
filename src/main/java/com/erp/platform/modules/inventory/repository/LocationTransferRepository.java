package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.LocationTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationTransferRepository extends JpaRepository<LocationTransfer, UUID> {

    Page<LocationTransfer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<LocationTransfer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<LocationTransfer> findByTenantIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID warehouseId, Pageable pageable);

    List<LocationTransfer> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    long countByTenantIdAndTransferNumberStartingWith(UUID tenantId, String prefix);
}
