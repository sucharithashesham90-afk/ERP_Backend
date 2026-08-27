package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessTemplateRepository extends JpaRepository<ProcessTemplate, UUID> {

    Page<ProcessTemplate> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProcessTemplate> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
