package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SubAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubAccountRepository extends JpaRepository<SubAccount, UUID> {

    List<SubAccount> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<SubAccount> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SubAccount> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
