package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<JournalEntry> {

    Page<JournalEntry> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<JournalEntry> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, JEStatus status, Pageable pageable);

    Optional<JournalEntry> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** True if a journal entry already exists for this source document (used to prevent duplicate vouchers). */
    boolean existsByTenantIdAndReferenceIdAndReferenceTypeInAndDeletedAtIsNull(
            UUID tenantId, UUID referenceId, java.util.Collection<String> referenceTypes);

    boolean existsByTenantIdAndReferenceNumberAndReferenceTypeInAndDeletedAtIsNull(
            UUID tenantId, String referenceNumber, java.util.Collection<String> referenceTypes);

    /** Guards the voucher-book number series against handing out a number that is already in use. */
    boolean existsByTenantIdAndEntryNumberAndDeletedAtIsNull(UUID tenantId, String entryNumber);

    List<JournalEntry> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    /** Every entry raised against one kind of source document, oldest first. */
    List<JournalEntry> findByTenantIdAndReferenceTypeAndDeletedAtIsNullOrderByCreatedAtAsc(
            UUID tenantId, String referenceType);

    /** True when vouchers have already been numbered out of this book (blocks deleting it). */
    boolean existsByTenantIdAndVoucherBookIdAndDeletedAtIsNull(UUID tenantId, UUID voucherBookId);

    @Query("SELECT jel.accountCode, jel.accountName, SUM(jel.debitAmount), SUM(jel.creditAmount) " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to AND je.deletedAt IS NULL " +
           "GROUP BY jel.accountCode, jel.accountName ORDER BY jel.accountCode")
    List<Object[]> trialBalance(@Param("tenantId") UUID tenantId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(jel.debitAmount), 0) FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to " +
           "AND jel.accountCode = :accountCode AND je.deletedAt IS NULL")
    java.math.BigDecimal sumDebitsByAccountAndPeriod(@Param("tenantId") UUID tenantId,
                                                     @Param("accountCode") String accountCode,
                                                     @Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(jel.creditAmount), 0) FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to " +
           "AND jel.accountCode = :accountCode AND je.deletedAt IS NULL")
    java.math.BigDecimal sumCreditsByAccountAndPeriod(@Param("tenantId") UUID tenantId,
                                                      @Param("accountCode") String accountCode,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to);

    @Query("SELECT jel.debitAmount, jel.creditAmount, je.entryDate, je.id " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to " +
           "AND jel.accountCode = :accountCode AND je.deletedAt IS NULL")
    List<Object[]> findLinesByAccountAndPeriod(@Param("tenantId") UUID tenantId,
                                               @Param("accountCode") String accountCode,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

    @Query("SELECT DISTINCT je FROM JournalEntry je JOIN je.lines jel " +
           "WHERE je.tenantId = :tenantId AND jel.accountId = :accountId " +
           "AND je.deletedAt IS NULL ORDER BY je.entryDate DESC")
    org.springframework.data.domain.Page<JournalEntry> findByTenantIdAndLineAccountId(
            @Param("tenantId") UUID tenantId,
            @Param("accountId") UUID accountId,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT jel.debitAmount, jel.creditAmount, je.entryDate, je.description, " +
           "je.referenceNumber, je.referenceType, je.entryNumber " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to " +
           "AND (jel.accountCode = :accountCode OR (:accountId IS NOT NULL AND jel.accountId = :accountId) OR LOWER(jel.accountName) = LOWER(:accountName)) AND je.deletedAt IS NULL " +
           "ORDER BY je.entryDate ASC, je.createdAt ASC")
    List<Object[]> findAccountStatementFlexible(@Param("tenantId") UUID tenantId,
                                                @Param("accountCode") String accountCode,
                                                @Param("accountId") UUID accountId,
                                                @Param("accountName") String accountName,
                                                @Param("from") LocalDate from,
                                                @Param("to") LocalDate to);

    @Query("SELECT jel.debitAmount, jel.creditAmount, je.entryDate, je.description, " +
           "je.referenceNumber, je.referenceType, je.entryNumber " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate >= :from AND je.entryDate <= :to " +
           "AND jel.accountCode = :accountCode AND je.deletedAt IS NULL " +
           "ORDER BY je.entryDate ASC, je.createdAt ASC")
    List<Object[]> findAccountStatement(@Param("tenantId") UUID tenantId,
                                        @Param("accountCode") String accountCode,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(jel.debitAmount) - SUM(jel.creditAmount), 0) " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate < :from " +
           "AND (jel.accountCode = :accountCode OR (:accountId IS NOT NULL AND jel.accountId = :accountId) OR LOWER(jel.accountName) = LOWER(:accountName)) AND je.deletedAt IS NULL")
    java.math.BigDecimal getOpeningBalanceFlexible(@Param("tenantId") UUID tenantId,
                                                   @Param("accountCode") String accountCode,
                                                   @Param("accountId") UUID accountId,
                                                   @Param("accountName") String accountName,
                                                   @Param("from") LocalDate from);

    @Query("SELECT COALESCE(SUM(jel.debitAmount) - SUM(jel.creditAmount), 0) " +
           "FROM JournalEntryLine jel JOIN jel.journalEntry je " +
           "WHERE je.tenantId = :tenantId AND je.status = 'POSTED' " +
           "AND je.entryDate < :from " +
           "AND jel.accountCode = :accountCode AND je.deletedAt IS NULL")
    java.math.BigDecimal getOpeningBalance(@Param("tenantId") UUID tenantId,
                                           @Param("accountCode") String accountCode,
                                           @Param("from") LocalDate from);

    @Query("SELECT je FROM JournalEntry je WHERE je.tenantId = :tenantId " +
           "AND je.deletedAt IS NULL " +
           "AND (:status IS NULL OR je.status = :status) " +
           "AND (:refType IS NULL OR je.referenceType = :refType) " +
           "AND (:from IS NULL OR je.entryDate >= :from) " +
           "AND (:to IS NULL OR je.entryDate <= :to) " +
           "ORDER BY je.entryDate DESC, je.createdAt DESC")
    org.springframework.data.domain.Page<JournalEntry> searchJournalEntries(
            @Param("tenantId") UUID tenantId,
            @Param("status") JEStatus status,
            @Param("refType") String refType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            org.springframework.data.domain.Pageable pageable);
}
