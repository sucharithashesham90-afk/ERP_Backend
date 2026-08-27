package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.LotPurchaseReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotPurchaseReturnRepository extends JpaRepository<LotPurchaseReturn, UUID> {
    Page<LotPurchaseReturn> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<LotPurchaseReturn> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<LotPurchaseReturn> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
