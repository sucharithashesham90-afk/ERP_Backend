package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.CreditNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {
    Page<CreditNote> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<CreditNote> findByTenantIdAndNoteTypeAndDeletedAtIsNull(UUID tenantId, String noteType, Pageable pageable);
    Optional<CreditNote> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** Guards against a second note for the same return if it is approved more than once. */
    boolean existsByTenantIdAndSalesReturnIdAndDeletedAtIsNull(UUID tenantId, UUID salesReturnId);
}
