package com.erp.platform.modules.hr.repository;
import com.erp.platform.modules.hr.entity.StatutoryDeduction;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface StatutoryDeductionRepository extends JpaRepository<StatutoryDeduction,UUID> {
    Page<StatutoryDeduction> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<StatutoryDeduction> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
