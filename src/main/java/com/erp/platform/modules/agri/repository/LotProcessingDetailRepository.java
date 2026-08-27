package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.LotProcessingDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotProcessingDetailRepository extends JpaRepository<LotProcessingDetail, UUID> {
    Page<LotProcessingDetail> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<LotProcessingDetail> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<LotProcessingDetail> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
