package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.ProductBomLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductBomLineRepository extends JpaRepository<ProductBomLine, UUID> {

    List<ProductBomLine> findByTenantIdAndProductIdAndDeletedAtIsNullOrderBySortOrderAscCreatedAtAsc(
            UUID tenantId, UUID productId);

    List<ProductBomLine> findByTenantIdAndBrandIdAndDeletedAtIsNullOrderBySortOrderAscCreatedAtAsc(
            UUID tenantId, UUID brandId);

    Optional<ProductBomLine> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
