package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, UUID> {
    Page<PurchaseReturn> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PurchaseReturn> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
