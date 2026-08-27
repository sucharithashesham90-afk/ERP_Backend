package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessOrderRepository extends JpaRepository<ProcessOrder, UUID> {

    Page<ProcessOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<ProcessOrder> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, String status, Pageable pageable);

    Optional<ProcessOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT p FROM ProcessOrder p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL AND (p.locationName IN :locations OR p.locationName IS NULL)")
    Page<ProcessOrder> findByTenantIdAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("locations") List<String> locations, Pageable pageable);

    @Query("SELECT p FROM ProcessOrder p WHERE p.tenantId = :tenantId AND p.status = :status AND p.deletedAt IS NULL AND (p.locationName IN :locations OR p.locationName IS NULL)")
    Page<ProcessOrder> findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("status") String status, @Param("locations") List<String> locations, Pageable pageable);
}
