package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.UnitOfMeasure;
import com.erp.platform.modules.master.entity.UnitOfMeasure.UoMType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {

    Page<UnitOfMeasure> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<UnitOfMeasure> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<UnitOfMeasure> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    Page<UnitOfMeasure> findByTenantIdAndUomTypeAndDeletedAtIsNull(UUID tenantId, UoMType uomType, Pageable pageable);

    List<UnitOfMeasure> findByTenantIdAndDeletedAtIsNullOrderByCodeAsc(UUID tenantId);

    long countByTenantId(UUID tenantId);
}
