package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.Net;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NetRepository extends JpaRepository<Net, UUID> {

    Page<Net> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Net> findByTenantIdAndGodownIdAndDeletedAtIsNull(UUID tenantId, UUID godownId, Pageable pageable);

    Optional<Net> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
