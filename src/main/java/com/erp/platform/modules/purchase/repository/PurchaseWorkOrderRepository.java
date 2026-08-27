package com.erp.platform.modules.purchase.repository;
import com.erp.platform.modules.purchase.entity.PurchaseWorkOrder;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface PurchaseWorkOrderRepository extends JpaRepository<PurchaseWorkOrder,UUID> {
    Page<PurchaseWorkOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<PurchaseWorkOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
