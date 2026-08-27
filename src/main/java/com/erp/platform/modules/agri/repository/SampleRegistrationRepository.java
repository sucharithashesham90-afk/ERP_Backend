package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SampleRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleRegistrationRepository extends JpaRepository<SampleRegistration, UUID> {

    List<SampleRegistration> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<SampleRegistration> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SampleRegistration> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<SampleRegistration> findByTenantIdAndIdInAndDeletedAtIsNull(UUID tenantId, List<UUID> ids);
}
