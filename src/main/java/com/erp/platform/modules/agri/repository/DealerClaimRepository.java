package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.DealerClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DealerClaimRepository extends JpaRepository<DealerClaim, UUID> {
    Page<DealerClaim> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<DealerClaim> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
