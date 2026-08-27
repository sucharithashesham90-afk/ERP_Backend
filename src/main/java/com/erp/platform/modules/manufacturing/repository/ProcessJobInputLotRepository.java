package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessJobInputLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessJobInputLotRepository extends JpaRepository<ProcessJobInputLot, UUID> {
    List<ProcessJobInputLot> findByJobIdOrderBySortOrder(UUID jobId);
}
