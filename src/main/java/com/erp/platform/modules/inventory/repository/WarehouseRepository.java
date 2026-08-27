package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Page<Warehouse> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Warehouse> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Warehouse> findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(UUID tenantId);

    List<Warehouse> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    List<Warehouse> findByTenantIdAndActiveTrueAndLocationAndDeletedAtIsNull(UUID tenantId, String location);

    @Query("SELECT DISTINCT w.location FROM Warehouse w WHERE w.tenantId = :tenantId AND w.location IS NOT NULL AND w.location <> '' AND w.deletedAt IS NULL ORDER BY w.location")
    List<String> findDistinctLocationsByTenantId(@Param("tenantId") UUID tenantId);
}
