package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<ProductCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
