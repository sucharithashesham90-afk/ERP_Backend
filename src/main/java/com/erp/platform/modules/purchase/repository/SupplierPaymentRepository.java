package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.SupplierPayment;
import com.erp.platform.modules.purchase.entity.SupplierPayment.PayStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, UUID> {

    Page<SupplierPayment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SupplierPayment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);

    Page<SupplierPayment> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);

    Page<SupplierPayment> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, PayStatus status, Pageable pageable);

    Page<SupplierPayment> findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID vendorId, PayStatus status, Pageable pageable);
}
