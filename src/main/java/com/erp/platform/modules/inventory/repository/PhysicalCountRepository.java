package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.PhysicalCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PhysicalCountRepository extends JpaRepository<PhysicalCount, UUID> {
    Page<PhysicalCount> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PhysicalCount> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
