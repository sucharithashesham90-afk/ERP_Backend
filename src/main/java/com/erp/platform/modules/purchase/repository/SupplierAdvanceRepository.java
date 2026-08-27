package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.entity.SupplierAdvance.AdvanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierAdvanceRepository extends JpaRepository<SupplierAdvance, UUID> {

    Page<SupplierAdvance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<SupplierAdvance> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);

    Optional<SupplierAdvance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<SupplierAdvance> findByTenantIdAndVendorIdAndStatusNotAndDeletedAtIsNull(UUID tenantId, UUID vendorId, AdvanceStatus status);
}
