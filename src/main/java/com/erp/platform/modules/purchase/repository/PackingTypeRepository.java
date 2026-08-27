package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PackingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackingTypeRepository extends JpaRepository<PackingType, UUID> {
    Page<PackingType> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PackingType> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
