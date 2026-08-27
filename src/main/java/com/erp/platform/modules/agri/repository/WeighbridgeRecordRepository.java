package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.WeighbridgeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeighbridgeRecordRepository extends JpaRepository<WeighbridgeRecord, UUID> {
    Page<WeighbridgeRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<WeighbridgeRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
