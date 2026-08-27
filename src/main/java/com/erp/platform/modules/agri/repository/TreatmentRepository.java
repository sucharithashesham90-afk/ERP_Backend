package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {

    Page<Treatment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Treatment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
