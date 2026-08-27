package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {

    Page<Receipt> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Receipt> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
