package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.CropGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropGroupRepository extends JpaRepository<CropGroup, UUID> {

    List<CropGroup> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<CropGroup> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<CropGroup> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
