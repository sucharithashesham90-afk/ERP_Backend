package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Farmer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FarmerRepository extends JpaRepository<Farmer, UUID> {

    Page<Farmer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Farmer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
