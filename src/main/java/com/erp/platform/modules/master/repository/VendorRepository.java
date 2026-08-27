package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    Page<Vendor> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Vendor> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndEmailAndDeletedAtIsNull(UUID tenantId, String email);

    @Query("SELECT v FROM Vendor v WHERE v.tenantId = :tenantId AND v.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(v.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
    Page<Vendor> searchByTenantId(@Param("tenantId") UUID tenantId,
                                  @Param("search") String search,
                                  Pageable pageable);
}
