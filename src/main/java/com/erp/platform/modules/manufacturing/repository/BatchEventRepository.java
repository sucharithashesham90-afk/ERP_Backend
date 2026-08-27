package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.BatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BatchEventRepository extends JpaRepository<BatchEvent, UUID> {

    List<BatchEvent> findByBatchIdOrderByEventDateDesc(UUID batchId);
}
