package com.erp.platform.modules.master.repository;
import com.erp.platform.modules.master.entity.Currency;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
import java.util.List;
public interface CurrencyRepository extends JpaRepository<Currency,UUID> {
    Page<Currency> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<Currency> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
    List<Currency> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String code);
    boolean existsByTenantIdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, String code, UUID id);
}
