package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.AuthorizationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationRequestRepository extends JpaRepository<AuthorizationRequest, UUID> {

    Page<AuthorizationRequest> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<AuthorizationRequest> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
