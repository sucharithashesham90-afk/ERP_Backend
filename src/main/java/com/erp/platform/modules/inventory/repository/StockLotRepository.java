package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.StockLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockLotRepository extends JpaRepository<StockLot, UUID> {

    Page<StockLot> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    /** Physical-inventory search — every filter is optional and narrows the result. */
    @Query("SELECT s FROM StockLot s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL " +
           "AND (:godownId IS NULL OR s.godownId = :godownId) " +
           "AND (:cropGroupName IS NULL OR s.cropGroupName = :cropGroupName) " +
           "AND (:cropName IS NULL OR s.cropName = :cropName) " +
           "AND (:varietyName IS NULL OR s.varietyName = :varietyName) " +
           "AND (:materialType IS NULL OR s.materialType = :materialType) " +
           "AND (:materialState IS NULL OR s.materialState = :materialState)")
    Page<StockLot> search(@Param("tenantId") UUID tenantId,
                          @Param("godownId") UUID godownId,
                          @Param("cropGroupName") String cropGroupName,
                          @Param("cropName") String cropName,
                          @Param("varietyName") String varietyName,
                          @Param("materialType") String materialType,
                          @Param("materialState") String materialState,
                          Pageable pageable);

    List<StockLot> findByTenantIdAndLotNoAndDeletedAtIsNull(UUID tenantId, String lotNo);

    Page<StockLot> findByTenantIdAndGodownIdAndDeletedAtIsNull(UUID tenantId, UUID godownId, Pageable pageable);

    Optional<StockLot> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<StockLot> findFirstByTenantIdAndLotNoAndDeletedAtIsNullOrderByCreatedAtAsc(UUID tenantId, String lotNo);
}
