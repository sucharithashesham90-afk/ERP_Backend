package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.DiscountDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscountDefinitionRepository extends JpaRepository<DiscountDefinition, UUID> {
    Page<DiscountDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<DiscountDefinition> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<DiscountDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
