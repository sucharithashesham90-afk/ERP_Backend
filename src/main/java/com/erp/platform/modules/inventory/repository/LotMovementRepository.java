package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.LotMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotMovementRepository extends JpaRepository<LotMovement, UUID> {

    Page<LotMovement> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<LotMovement> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<LotMovement> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber, Pageable pageable);

    List<LotMovement> findByTenantIdAndLotNumberAndDeletedAtIsNullOrderByMovementDateAscCreatedAtAsc(UUID tenantId, String lotNumber);

    List<LotMovement> findByTenantIdAndProductIdAndDeletedAtIsNullOrderByMovementDateAscCreatedAtAsc(UUID tenantId, UUID productId);
}
