package com.erp.platform.modules.master.repository;
import com.erp.platform.modules.master.entity.LotSeries;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface LotSeriesRepository extends JpaRepository<LotSeries,UUID> {
    Page<LotSeries> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<LotSeries> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
