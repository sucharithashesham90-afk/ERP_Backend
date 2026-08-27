package com.erp.platform.modules.accounting.repository;
import com.erp.platform.modules.accounting.entity.ShareCapital;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface ShareCapitalRepository extends JpaRepository<ShareCapital,UUID> {
    Page<ShareCapital> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<ShareCapital> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
