package com.erp.platform.modules.organization.repository;

import com.erp.platform.modules.organization.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Page<Company> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<Company> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
