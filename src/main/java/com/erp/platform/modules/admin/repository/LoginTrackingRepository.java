package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.LoginTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginTrackingRepository extends JpaRepository<LoginTracking, UUID> {

    Page<LoginTracking> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<LoginTracking> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
