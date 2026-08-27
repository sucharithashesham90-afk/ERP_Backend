package com.erp.platform.modules.supplier.repository;

import com.erp.platform.modules.supplier.entity.SupplierContract;
import com.erp.platform.modules.supplier.entity.SupplierContract.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierContractRepository extends JpaRepository<SupplierContract, UUID> {

    Page<SupplierContract> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SupplierContract> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SupplierContract> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);

    Page<SupplierContract> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ContractStatus status, Pageable pageable);

    Page<SupplierContract> findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID vendorId, ContractStatus status, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
