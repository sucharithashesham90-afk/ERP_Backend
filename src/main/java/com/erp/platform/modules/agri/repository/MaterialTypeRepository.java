package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.MaterialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialTypeRepository extends JpaRepository<MaterialType, UUID> {

    Page<MaterialType> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<MaterialType> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<MaterialType> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
