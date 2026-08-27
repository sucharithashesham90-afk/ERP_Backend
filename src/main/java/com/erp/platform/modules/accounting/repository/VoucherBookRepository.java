package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.VoucherBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherBookRepository extends JpaRepository<VoucherBook, UUID> {
    Page<VoucherBook> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<VoucherBook> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<VoucherBook> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    Optional<VoucherBook> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    /**
     * findFirst (not findBy) — legacy tenants can still hold duplicate rows for the same code, and an
     * Optional-returning findBy blows up with IncorrectResultSizeDataAccessException on those, which
     * would fail every posting that needs to resolve a book.
     */
    Optional<VoucherBook> findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
            UUID tenantId, String code);
}
