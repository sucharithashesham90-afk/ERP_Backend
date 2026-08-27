package com.erp.platform.modules.dispatch.repository;

import com.erp.platform.modules.dispatch.entity.DispatchChallan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispatchChallanRepository extends JpaRepository<DispatchChallan, UUID> {

    Page<DispatchChallan> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DispatchChallan> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<DispatchChallan> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    /** Challans raised against one customer, newest first — the dealer-transfer "from" picker. */
    List<DispatchChallan> findByTenantIdAndCustomerNameIgnoreCaseAndDeletedAtIsNullOrderByChallanDateDesc(
            UUID tenantId, String customerName);
}
