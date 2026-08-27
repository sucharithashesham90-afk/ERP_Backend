package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.Dimension;
import com.erp.platform.modules.accounting.entity.DimensionValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DimensionValueRepository extends JpaRepository<DimensionValue, UUID> {

    Page<DimensionValue> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DimensionValue> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<DimensionValue> findByTenantIdAndDimensionAndDeletedAtIsNull(UUID tenantId, Dimension dimension, Pageable pageable);
}
