package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.BagSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BagSizeRepository extends JpaRepository<BagSize, UUID> {
    Page<BagSize> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<BagSize> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
