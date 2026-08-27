package com.erp.platform.modules.accounting.repository;
import com.erp.platform.modules.accounting.entity.Shareholder;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface ShareholderRepository extends JpaRepository<Shareholder,UUID> {
    Page<Shareholder> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<Shareholder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
