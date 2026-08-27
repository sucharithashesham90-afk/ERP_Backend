package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.TdsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TdsCategoryRepository extends JpaRepository<TdsCategory, UUID> {
    Page<TdsCategory> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<TdsCategory> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<TdsCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
