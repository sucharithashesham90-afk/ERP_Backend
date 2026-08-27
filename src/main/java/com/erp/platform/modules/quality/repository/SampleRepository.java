package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.Sample;
import com.erp.platform.modules.quality.entity.Sample.SampleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleRepository extends JpaRepository<Sample, UUID> {

    Page<Sample> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<Sample> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    Optional<Sample> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<Sample> findByTenantIdAndIdInAndDeletedAtIsNull(UUID tenantId, List<UUID> ids);

    Page<Sample> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, SampleStatus status, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);

    /**
     * Lot Search over recorded results.
     *
     * <p>Results are entered against samples (Result Entry writes {@code resultStatus} /
     * {@code resultsJson} on the sample), so this — not the quality_test_results table — is where
     * Lot Search has to look. Blank lot/product and null from/to/result mean "no filter".
     */
    @Query("SELECT s FROM Sample s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL "
         + "AND (:lot = '' OR LOWER(COALESCE(s.lotNumber, '')) LIKE LOWER(CONCAT('%', CAST(:lot AS String), '%'))) "
         + "AND (:product = '' OR LOWER(COALESCE(s.productName, '')) LIKE LOWER(CONCAT('%', CAST(:product AS String), '%')) "
         + "     OR LOWER(COALESCE(s.cropName, '')) LIKE LOWER(CONCAT('%', CAST(:product AS String), '%')) "
         + "     OR LOWER(COALESCE(s.varietyName, '')) LIKE LOWER(CONCAT('%', CAST(:product AS String), '%'))) "
         + "AND (:from IS NULL OR s.sampleDate >= :from) "
         + "AND (:to IS NULL OR s.sampleDate <= :to) "
         + "AND (:result = '' OR UPPER(COALESCE(s.resultStatus, 'PENDING')) = UPPER(CAST(:result AS String))) "
         + "ORDER BY s.sampleDate DESC")
    Page<Sample> searchResults(@Param("tenantId") UUID tenantId, @Param("lot") String lot,
            @Param("product") String product, @Param("from") LocalDate from,
            @Param("to") LocalDate to, @Param("result") String result, Pageable pageable);
}
