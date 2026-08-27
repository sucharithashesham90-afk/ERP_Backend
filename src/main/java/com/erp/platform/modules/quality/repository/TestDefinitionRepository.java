package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.TestDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestDefinitionRepository extends JpaRepository<TestDefinition, UUID> {

    Page<TestDefinition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<TestDefinition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
