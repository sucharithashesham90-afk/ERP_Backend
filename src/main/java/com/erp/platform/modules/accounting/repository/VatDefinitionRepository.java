package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.VatDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VatDefinitionRepository extends JpaRepository<VatDefinition, UUID> {
    Page<VatDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<VatDefinition> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<VatDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
