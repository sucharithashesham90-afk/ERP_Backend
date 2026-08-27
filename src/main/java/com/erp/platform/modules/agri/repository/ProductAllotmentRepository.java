package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProductAllotment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductAllotmentRepository extends JpaRepository<ProductAllotment, UUID> {
    List<ProductAllotment> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<ProductAllotment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<ProductAllotment> findByTenantIdAndDealerIdAndDeletedAtIsNull(UUID tenantId, UUID dealerId);
}
