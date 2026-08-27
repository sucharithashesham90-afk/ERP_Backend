package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.IntakeLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IntakeLineRepository extends JpaRepository<IntakeLine, UUID> {
    List<IntakeLine> findByIntakeIdOrderByLineNumber(UUID intakeId);
}
