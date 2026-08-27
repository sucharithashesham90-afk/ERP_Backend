package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxRepository extends JpaRepository<Tax, UUID> {
    List<Tax> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<Tax> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
