package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AccountHead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountHeadRepository extends JpaRepository<AccountHead, UUID> {
    Page<AccountHead> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AccountHead> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    List<AccountHead> findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNull(UUID tenantId, String type);
    Optional<AccountHead> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
