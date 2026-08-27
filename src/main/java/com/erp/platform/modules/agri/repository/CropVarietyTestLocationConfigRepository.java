package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.CropVarietyTestLocationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CropVarietyTestLocationConfigRepository extends JpaRepository<CropVarietyTestLocationConfig, UUID> {
}
