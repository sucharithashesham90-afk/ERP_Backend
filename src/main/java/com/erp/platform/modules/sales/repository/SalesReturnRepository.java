package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, UUID> {
    Page<SalesReturn> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<SalesReturn> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT DISTINCT r FROM SalesReturn r JOIN r.items i " +
           "WHERE r.tenantId = :tenantId AND i.lotNumber = :lotNumber AND r.deletedAt IS NULL")
    List<SalesReturn> findByLotNumber(@Param("tenantId") UUID tenantId, @Param("lotNumber") String lotNumber);
}
