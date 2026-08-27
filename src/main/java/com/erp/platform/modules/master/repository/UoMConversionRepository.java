package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.UoMConversion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UoMConversionRepository extends JpaRepository<UoMConversion, UUID> {

    Page<UoMConversion> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<UoMConversion> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<UoMConversion> findByTenantIdAndFromUomIdAndDeletedAtIsNull(UUID tenantId, UUID fromUomId, Pageable pageable);

    Optional<UoMConversion> findByTenantIdAndFromUomIdAndToUomIdAndDeletedAtIsNull(UUID tenantId, UUID fromUomId, UUID toUomId);
}
