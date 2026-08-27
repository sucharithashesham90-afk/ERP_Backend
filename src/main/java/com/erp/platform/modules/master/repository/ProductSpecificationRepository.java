package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, UUID> {

    List<ProductSpecification> findByProductIdAndDeletedAtIsNullOrderByDisplayOrder(UUID productId);

    Page<ProductSpecification> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProductSpecification> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    void deleteByProductId(UUID productId);
}
