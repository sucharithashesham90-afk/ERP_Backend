package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.SupplierPaymentAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierPaymentAllocationRepository extends JpaRepository<SupplierPaymentAllocation, UUID> {

    Page<SupplierPaymentAllocation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SupplierPaymentAllocation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
