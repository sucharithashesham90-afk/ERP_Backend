package com.erp.platform.modules.sales.repository;
import com.erp.platform.modules.sales.entity.CustomerDiscount;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface CustomerDiscountRepository extends JpaRepository<CustomerDiscount,UUID> {
    Page<CustomerDiscount> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<CustomerDiscount> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
