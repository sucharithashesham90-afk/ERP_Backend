package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Customer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndEmailAndDeletedAtIsNull(UUID tenantId, String email);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    /**
     * A customer by name — what the dealer-transfer screens carry, since they hold both parties as
     * free-text names rather than references.
     */
    Optional<Customer> findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(c.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
    Page<Customer> searchByTenantId(@Param("tenantId") UUID tenantId,
                                    @Param("search") String search,
                                    Pageable pageable);
}
