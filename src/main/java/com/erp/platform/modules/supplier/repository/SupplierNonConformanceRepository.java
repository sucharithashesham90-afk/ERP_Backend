package com.erp.platform.modules.supplier.repository;

import com.erp.platform.modules.supplier.entity.SupplierNonConformance;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierNonConformanceRepository extends JpaRepository<SupplierNonConformance, UUID> {

    Page<SupplierNonConformance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SupplierNonConformance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SupplierNonConformance> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, NCStatus status, Pageable pageable);

    Page<SupplierNonConformance> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);

    Page<SupplierNonConformance> findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID vendorId, NCStatus status, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
