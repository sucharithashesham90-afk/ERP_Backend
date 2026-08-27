package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.VarietyRelease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VarietyReleaseRepository extends JpaRepository<VarietyRelease, UUID> {
    Page<VarietyRelease> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<VarietyRelease> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
