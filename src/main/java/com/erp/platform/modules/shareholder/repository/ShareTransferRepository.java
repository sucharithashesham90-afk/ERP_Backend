package com.erp.platform.modules.shareholder.repository;

import com.erp.platform.modules.shareholder.entity.ShareTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShareTransferRepository extends JpaRepository<ShareTransfer, UUID> {

    Page<ShareTransfer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ShareTransfer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
