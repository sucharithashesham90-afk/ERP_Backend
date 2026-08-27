package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.AccountDefault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountDefaultRepository extends JpaRepository<AccountDefault, UUID> {

    Page<AccountDefault> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<AccountDefault> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
