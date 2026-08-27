package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.CropData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropDataRepository extends JpaRepository<CropData, UUID> {
    List<CropData> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<CropData> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<CropData> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
