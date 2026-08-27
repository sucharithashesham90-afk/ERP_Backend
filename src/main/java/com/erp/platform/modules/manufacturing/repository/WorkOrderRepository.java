package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.WorkOrder;
import com.erp.platform.modules.manufacturing.entity.WorkOrder.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    Page<WorkOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<WorkOrder> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, WorkOrderStatus status, Pageable pageable);

    Optional<WorkOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT w FROM WorkOrder w WHERE w.tenantId = :tenantId AND w.deletedAt IS NULL AND (w.locationName IN :locations OR w.locationName IS NULL)")
    Page<WorkOrder> findByTenantIdAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("locations") List<String> locations, Pageable pageable);

    @Query("SELECT w FROM WorkOrder w WHERE w.tenantId = :tenantId AND w.status = :status AND w.deletedAt IS NULL AND (w.locationName IN :locations OR w.locationName IS NULL)")
    Page<WorkOrder> findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("status") WorkOrderStatus status, @Param("locations") List<String> locations, Pageable pageable);
}
