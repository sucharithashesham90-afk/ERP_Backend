package com.erp.platform.modules.hr.repository;
import com.erp.platform.modules.hr.entity.SalaryElement;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface SalaryElementRepository extends JpaRepository<SalaryElement,UUID> {
    Page<SalaryElement> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<SalaryElement> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
