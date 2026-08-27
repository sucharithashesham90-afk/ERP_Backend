package com.erp.platform.modules.organization.repository;

import com.erp.platform.modules.organization.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Page<Branch> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<Branch> findByTenantIdAndCompanyIdAndDeletedAtIsNull(UUID tenantId, UUID companyId, Pageable pageable);
    Optional<Branch> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** A branch code identifies a branch within a tenant, so it has to be unique among live ones. */
    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, String code, UUID id);

    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, String name, UUID id);

    Optional<Branch> findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String code);
}
