package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StorageLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, UUID> {

    Page<StorageLocation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<StorageLocation> findByTenantIdAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, UUID warehouseId, Pageable pageable);

    Optional<StorageLocation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<StorageLocation> findByTenantIdAndCodeAndWarehouseIdAndDeletedAtIsNull(UUID tenantId, String code, UUID warehouseId);

    Optional<StorageLocation> findFirstByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(UUID tenantId);
}
