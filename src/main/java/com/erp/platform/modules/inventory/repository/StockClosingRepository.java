package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockClosing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockClosingRepository extends JpaRepository<StockClosing, UUID> {
    Page<StockClosing> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
