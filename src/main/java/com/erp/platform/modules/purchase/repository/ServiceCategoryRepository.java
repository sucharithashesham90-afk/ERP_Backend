package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {

    Page<ServiceCategory> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ServiceCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
