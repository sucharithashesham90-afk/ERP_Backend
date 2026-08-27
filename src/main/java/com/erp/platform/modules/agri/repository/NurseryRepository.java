package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Nursery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NurseryRepository extends JpaRepository<Nursery, UUID> {

    Page<Nursery> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Nursery> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
