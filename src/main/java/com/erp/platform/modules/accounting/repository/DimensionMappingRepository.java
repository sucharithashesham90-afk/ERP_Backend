package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.DimensionMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DimensionMappingRepository extends JpaRepository<DimensionMapping, UUID> {

    Page<DimensionMapping> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DimensionMapping> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<DimensionMapping> findByTenantIdAndReferenceIdAndDeletedAtIsNull(UUID tenantId, UUID referenceId);
}
