package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessingScreen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessingScreenRepository extends JpaRepository<ProcessingScreen, UUID> {

    Page<ProcessingScreen> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProcessingScreen> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
