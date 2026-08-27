package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.GrowerPlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrowerPlotRepository extends JpaRepository<GrowerPlot, UUID> {
    Page<GrowerPlot> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<GrowerPlot> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
