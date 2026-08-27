package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.WasteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WasteTypeRepository extends JpaRepository<WasteType, UUID> {

    List<WasteType> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<WasteType> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<WasteType> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<WasteType> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
