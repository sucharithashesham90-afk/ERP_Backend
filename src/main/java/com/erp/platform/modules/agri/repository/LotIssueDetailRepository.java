package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.LotIssueDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotIssueDetailRepository extends JpaRepository<LotIssueDetail, UUID> {
    Page<LotIssueDetail> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<LotIssueDetail> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<LotIssueDetail> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
