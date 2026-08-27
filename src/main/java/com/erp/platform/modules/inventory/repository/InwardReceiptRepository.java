package com.erp.platform.modules.inventory.repository;
import com.erp.platform.modules.inventory.entity.InwardReceipt;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface InwardReceiptRepository extends JpaRepository<InwardReceipt,UUID> {
    Page<InwardReceipt> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<InwardReceipt> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
