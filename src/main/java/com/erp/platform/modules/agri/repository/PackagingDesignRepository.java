package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PackagingDesign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackagingDesignRepository extends JpaRepository<PackagingDesign, UUID> {

    Page<PackagingDesign> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PackagingDesign> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
