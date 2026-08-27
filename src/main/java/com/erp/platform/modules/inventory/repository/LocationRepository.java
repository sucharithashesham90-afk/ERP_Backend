package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Page<Location> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Location> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<Location> findByTenantIdAndActiveTrueAndDeletedAtIsNullOrderByName(UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, String name, UUID id);
}
