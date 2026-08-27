package com.erp.platform.modules.hr.repository;
import com.erp.platform.modules.hr.entity.PayCycle;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface PayCycleRepository extends JpaRepository<PayCycle,UUID> {
    Page<PayCycle> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<PayCycle> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
