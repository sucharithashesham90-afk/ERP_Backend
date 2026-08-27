package com.erp.platform.modules.shareholder.repository;

import com.erp.platform.modules.shareholder.entity.ShareHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShareHolderRepository extends JpaRepository<ShareHolder, UUID> {

    Page<ShareHolder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ShareHolder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndShareholderNumberAndDeletedAtIsNull(UUID tenantId, String shareholderNumber);

    long countByTenantId(UUID tenantId);
}
