package com.erp.platform.modules.dispatch.repository;

import com.erp.platform.modules.dispatch.entity.DispatchPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispatchPackageRepository extends JpaRepository<DispatchPackage, UUID> {

    Page<DispatchPackage> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DispatchPackage> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<DispatchPackage> findByDispatchId(UUID dispatchId);
}
