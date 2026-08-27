package com.erp.platform.modules.master.repository;
import com.erp.platform.modules.master.entity.UomCategory;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface UomCategoryRepository extends JpaRepository<UomCategory,UUID> {
    Page<UomCategory> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<UomCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
