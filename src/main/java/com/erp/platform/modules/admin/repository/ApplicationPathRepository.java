package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.ApplicationPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationPathRepository extends JpaRepository<ApplicationPath, UUID> {

    Page<ApplicationPath> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ApplicationPath> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
