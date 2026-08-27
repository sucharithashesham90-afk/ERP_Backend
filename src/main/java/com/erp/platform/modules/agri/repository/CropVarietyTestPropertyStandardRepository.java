package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.CropVarietyTestPropertyStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CropVarietyTestPropertyStandardRepository extends JpaRepository<CropVarietyTestPropertyStandard, UUID> {
}
