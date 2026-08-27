package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkuRepository extends JpaRepository<Sku, UUID> {
    Page<Sku> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<Sku> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<Sku> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
