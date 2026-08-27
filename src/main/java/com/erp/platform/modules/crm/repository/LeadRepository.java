package com.erp.platform.modules.crm.repository;

import com.erp.platform.modules.crm.entity.Lead;
import com.erp.platform.modules.crm.entity.Lead.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Lead> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, LeadStatus status, Pageable pageable);

    Optional<Lead> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT l FROM Lead l WHERE l.tenantId = :tenantId AND l.deletedAt IS NULL AND (" +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')) OR " +
           "LOWER(l.company) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')))")
    Page<Lead> searchByTenantId(@Param("tenantId") UUID tenantId, @Param("q") String q, Pageable pageable);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, LeadStatus status);
}
