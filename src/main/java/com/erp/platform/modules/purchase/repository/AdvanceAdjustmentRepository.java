package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.AdvanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdvanceAdjustmentRepository extends JpaRepository<AdvanceAdjustment, UUID> {

    List<AdvanceAdjustment> findByAdvanceId(UUID advanceId);
}
