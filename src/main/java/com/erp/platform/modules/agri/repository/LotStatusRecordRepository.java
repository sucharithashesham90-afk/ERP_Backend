package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.LotStatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotStatusRecordRepository extends JpaRepository<LotStatusRecord, UUID> {

    List<LotStatusRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<LotStatusRecord> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<LotStatusRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<LotStatusRecord> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
