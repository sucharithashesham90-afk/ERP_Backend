package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.FarmerLandRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FarmerLandRecordRepository extends JpaRepository<FarmerLandRecord, UUID> {

    List<FarmerLandRecord> findByTenantIdAndFarmerIdAndDeletedAtIsNull(UUID tenantId, UUID farmerId);

    void deleteByTenantIdAndFarmerId(UUID tenantId, UUID farmerId);
}
