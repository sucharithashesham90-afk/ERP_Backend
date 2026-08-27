package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.PeriodDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodDefinitionRepository extends JpaRepository<PeriodDefinition, UUID> {

    Page<PeriodDefinition> findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(UUID tenantId, Pageable pageable);

    List<PeriodDefinition> findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(UUID tenantId);

    Optional<PeriodDefinition> findFirstByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(UUID tenantId);

    Optional<PeriodDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndPeriodCodeAndDeletedAtIsNull(UUID tenantId, String periodCode);
}
