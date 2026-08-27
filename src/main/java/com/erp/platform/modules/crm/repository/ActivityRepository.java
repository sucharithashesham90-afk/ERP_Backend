package com.erp.platform.modules.crm.repository;

import com.erp.platform.modules.crm.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    Page<Activity> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Activity> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<Activity> findByTenantIdAndReferenceIdAndDeletedAtIsNull(UUID tenantId, UUID refId, Pageable pageable);
}
