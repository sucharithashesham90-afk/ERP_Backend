package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PaymentLiability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentLiabilityRepository extends JpaRepository<PaymentLiability, UUID> {
    Page<PaymentLiability> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PaymentLiability> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /**
     * Liabilities to offer for posting, narrowed by the Liability Payment filters.
     *
     * <p>Intake type is derived rather than stored: a lot-wise intake carries a lot number, a
     * truck-wise one does not. {@code intake} is LOT, TRUCK, or blank for both.
     *
     * <p>Settled rows are excluded — there is nothing left to post against them.
     */
    @Query("SELECT l FROM AgriPaymentLiability l WHERE l.tenantId = :tenantId AND l.deletedAt IS NULL "
         + "AND UPPER(COALESCE(l.status, 'PENDING')) <> 'PAID' "
         + "AND (:partyType = '' OR UPPER(COALESCE(l.partyType, '')) = UPPER(CAST(:partyType AS String))) "
         + "AND (:partyName = '' OR LOWER(COALESCE(l.partyName, '')) = LOWER(CAST(:partyName AS String))) "
         + "AND (:intake = '' "
         + "     OR (UPPER(CAST(:intake AS String)) = 'LOT'   AND l.lotNumber IS NOT NULL AND l.lotNumber <> '') "
         + "     OR (UPPER(CAST(:intake AS String)) = 'TRUCK' AND (l.lotNumber IS NULL OR l.lotNumber = ''))) "
         + "AND (:from IS NULL OR l.liabilityFromDate >= :from) "
         + "AND (:to IS NULL OR l.liabilityToDate <= :to) "
         + "ORDER BY l.partyName ASC, l.liabilityFromDate ASC")
    List<PaymentLiability> searchForPosting(@Param("tenantId") UUID tenantId,
            @Param("partyType") String partyType, @Param("partyName") String partyName,
            @Param("intake") String intake, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Distinct party names for the grower / organizer picker. */
    @Query("SELECT DISTINCT l.partyName FROM AgriPaymentLiability l WHERE l.tenantId = :tenantId "
         + "AND l.deletedAt IS NULL AND l.partyName IS NOT NULL AND l.partyName <> '' "
         + "AND (:partyType = '' OR UPPER(COALESCE(l.partyType, '')) = UPPER(CAST(:partyType AS String))) "
         + "ORDER BY l.partyName ASC")
    List<String> findPartyNames(@Param("tenantId") UUID tenantId, @Param("partyType") String partyType);
}
