package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.PhysicalInventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhysicalInventoryAdjustmentRepository extends JpaRepository<PhysicalInventoryAdjustment, UUID> {
}
