package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.CostCenterAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CostCenterAllocationRepository extends JpaRepository<CostCenterAllocation, UUID> {

    Page<CostCenterAllocation> findByCostCenterIdOrderByAllocationDateDesc(UUID costCenterId, Pageable pageable);

    List<CostCenterAllocation> findByCostCenterIdAndAllocationDateBetween(UUID costCenterId, LocalDate from, LocalDate to);
}
