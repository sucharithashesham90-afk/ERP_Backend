package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.ServiceDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceDefinitionRepository extends JpaRepository<ServiceDefinition, UUID> {

    Page<ServiceDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<ServiceDefinition> findByTenantIdAndCategoryIdAndDeletedAtIsNull(UUID tenantId, UUID categoryId, Pageable pageable);

    Optional<ServiceDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
