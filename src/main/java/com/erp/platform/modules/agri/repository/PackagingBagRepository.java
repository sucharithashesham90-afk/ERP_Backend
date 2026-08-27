package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PackagingBag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackagingBagRepository extends JpaRepository<PackagingBag, UUID> {

    Page<PackagingBag> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PackagingBag> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
