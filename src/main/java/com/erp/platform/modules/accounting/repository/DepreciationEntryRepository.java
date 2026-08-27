package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.DepreciationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepreciationEntryRepository extends JpaRepository<DepreciationEntry, UUID> {

    List<DepreciationEntry> findByAssetIdOrderByDepreciationDateDesc(UUID assetId);

    List<DepreciationEntry> findByAssetIdOrderByDepreciationDateAsc(UUID assetId);

    List<DepreciationEntry> findByTenantIdAndPostedAndDepreciationDateBefore(UUID tenantId, boolean posted, LocalDate date);

    Optional<DepreciationEntry> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    void deleteByAssetId(UUID assetId);

    List<DepreciationEntry> findByTenantIdAndPeriodYearAndPeriodMonthOrderByDepreciationDateAsc(
            UUID tenantId, int periodYear, int periodMonth);

    List<DepreciationEntry> findByTenantIdAndPeriodYearOrderByDepreciationDateAsc(
            UUID tenantId, int periodYear);
}
