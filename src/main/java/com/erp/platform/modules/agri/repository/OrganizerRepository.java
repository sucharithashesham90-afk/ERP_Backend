package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizerRepository extends JpaRepository<Organizer, UUID> {

    List<Organizer> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<Organizer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Organizer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
