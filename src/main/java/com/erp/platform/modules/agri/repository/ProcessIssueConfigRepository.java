package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessIssueConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessIssueConfigRepository extends JpaRepository<ProcessIssueConfig, UUID> {

    List<ProcessIssueConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<ProcessIssueConfig> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<ProcessIssueConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
