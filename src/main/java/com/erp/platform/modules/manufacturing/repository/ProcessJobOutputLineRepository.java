package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessJobOutputLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessJobOutputLineRepository extends JpaRepository<ProcessJobOutputLine, UUID> {
    List<ProcessJobOutputLine> findByJobIdOrderBySortOrder(UUID jobId);
}
