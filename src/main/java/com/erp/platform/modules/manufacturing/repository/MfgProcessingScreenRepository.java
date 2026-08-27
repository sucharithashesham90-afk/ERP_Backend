package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.MfgProcessingScreen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MfgProcessingScreenRepository extends JpaRepository<MfgProcessingScreen, UUID> {
    Page<MfgProcessingScreen> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<MfgProcessingScreen> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<MfgProcessingScreen> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
