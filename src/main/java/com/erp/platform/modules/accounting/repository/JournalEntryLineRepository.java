package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, UUID>, JpaSpecificationExecutor<JournalEntryLine> {

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountCodeOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
            UUID tenantId, JEStatus status, LocalDate startDate, LocalDate endDate, String accountCode);

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountIdOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
            UUID tenantId, JEStatus status, LocalDate startDate, LocalDate endDate, UUID accountId);

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountCode(
            UUID tenantId, JEStatus status, LocalDate beforeDate, String accountCode);

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountId(
            UUID tenantId, JEStatus status, LocalDate beforeDate, UUID accountId);

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateBetweenAndAccountNameIgnoreCaseOrderByJournalEntryEntryDateAscJournalEntryCreatedAtAsc(
            UUID tenantId, JEStatus status, LocalDate startDate, LocalDate endDate, String accountName);

    List<JournalEntryLine> findByJournalEntryTenantIdAndJournalEntryStatusAndJournalEntryDeletedAtIsNullAndJournalEntryEntryDateLessThanAndAccountNameIgnoreCase(
            UUID tenantId, JEStatus status, LocalDate beforeDate, String accountName);
}
