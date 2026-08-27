package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.ProductLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductLineRepository extends JpaRepository<ProductLine, UUID> {

    Page<ProductLine> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProductLine> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<ProductLine> findByTenantIdAndBrandIdAndDeletedAtIsNull(UUID tenantId, UUID brandId, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
