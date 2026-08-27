package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Page<Account> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<Account> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    List<Account> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    List<Account> findByTenantIdAndTypeAndDeletedAtIsNull(UUID tenantId, String type);

    Page<Account> findByTenantIdAndTypeAndDeletedAtIsNull(UUID tenantId, String type, Pageable pageable);

    Optional<Account> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Account> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    List<Account> findByTenantIdAndSubTypeAndDeletedAtIsNull(UUID tenantId, String subType);

    Optional<Account> findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);
}
