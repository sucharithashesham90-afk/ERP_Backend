package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AccountGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, UUID> {
    Page<AccountGroup> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AccountGroup> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<AccountGroup> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
    Optional<AccountGroup> findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);
    Optional<AccountGroup> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    /**
     * Includes soft-deleted rows, because the unique index on (tenant_id, code) does not exclude
     * them. Looking only at live rows lets a deleted group's code be re-inserted, and the database
     * rejects it — surfacing as a duplicate error on whatever unrelated record triggered the
     * lookup. Callers should revive what this finds rather than insert alongside it.
     */
    Optional<AccountGroup> findFirstByTenantIdAndCode(UUID tenantId, String code);
}
