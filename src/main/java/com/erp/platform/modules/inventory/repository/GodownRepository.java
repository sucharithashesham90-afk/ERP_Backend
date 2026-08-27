package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.Godown;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GodownRepository extends JpaRepository<Godown, UUID> {

    Page<Godown> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Godown> findByTenantIdAndLocationAndDeletedAtIsNull(UUID tenantId, String location, Pageable pageable);

    Optional<Godown> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Godown> findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);
}
