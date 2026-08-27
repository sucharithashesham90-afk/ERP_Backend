package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.Dimension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DimensionRepository extends JpaRepository<Dimension, UUID> {

    Page<Dimension> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Dimension> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Dimension> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
