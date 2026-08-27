package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SalesScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesSchemeRepository extends JpaRepository<SalesScheme, UUID> {
    List<SalesScheme> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<SalesScheme> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
