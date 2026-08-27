package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.QualityTestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface QualityTestResultRepository extends JpaRepository<QualityTestResult, UUID> {
    Page<QualityTestResult> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<QualityTestResult> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    // Lot Search filters — blank lot/product and null from/to mean "no filter".
    @Query("SELECT q FROM AgriQualityTestResult q WHERE q.tenantId = :tenantId AND q.deletedAt IS NULL " +
           "AND (:lot = '' OR LOWER(COALESCE(q.lotNumber, '')) LIKE LOWER(CONCAT('%', CAST(:lot AS String), '%'))) " +
           "AND (:product = '' OR LOWER(COALESCE(q.cropName, '')) LIKE LOWER(CONCAT('%', CAST(:product AS String), '%')) " +
           "     OR LOWER(COALESCE(q.varietyName, '')) LIKE LOWER(CONCAT('%', CAST(:product AS String), '%'))) " +
           "AND (:from IS NULL OR q.testDate >= :from) " +
           "AND (:to IS NULL OR q.testDate <= :to)")
    Page<QualityTestResult> search(@Param("tenantId") UUID tenantId, @Param("lot") String lot,
            @Param("product") String product, @Param("from") LocalDate from,
            @Param("to") LocalDate to, Pageable pageable);
}
