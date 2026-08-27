package com.erp.platform.modules.sales.repository;
import java.util.UUID;

import com.erp.platform.modules.sales.entity.CustomerComplaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerComplaintRepository extends JpaRepository<CustomerComplaint, UUID> {
    Page<CustomerComplaint> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<CustomerComplaint> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
