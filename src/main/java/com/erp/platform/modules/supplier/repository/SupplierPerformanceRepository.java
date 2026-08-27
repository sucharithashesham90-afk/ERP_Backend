package com.erp.platform.modules.supplier.repository;

import com.erp.platform.modules.supplier.entity.SupplierPerformance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierPerformanceRepository extends JpaRepository<SupplierPerformance, UUID> {

    Page<SupplierPerformance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SupplierPerformance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SupplierPerformance> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);
}
