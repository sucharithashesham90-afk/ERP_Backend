package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.sales.entity.SalesReturn;
import com.erp.platform.modules.sales.repository.SalesReturnRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Clears up the sales-return credit notes that were posted more than once.
 *
 * <p>A credit note used to be raised when a return was <em>created</em> and again when it was
 * <em>approved</em>, and the posting itself had no memory of having run. So an approved return
 * carries two identical credits and a return still sitting in DRAFT carries one it should never
 * have had — a draft is a claim that goods are coming back, not an acceptance of them. Both are
 * fixed at the source; this endpoint deals with the entries already in the ledger.
 *
 * <p>Entries are <strong>cancelled, never deleted</strong>. Posting moved account balances, so
 * removing the row would leave the balance overstated with nothing to show why. Cancelling reverses
 * the balance and leaves the entry visible with its history intact, which is what an auditor needs
 * to see.
 *
 * <p>Defaults to a dry run. Call with {@code apply=true} once the report reads the way you expect.
 */
@RestController
@RequestMapping("/api/v1/sales/returns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales - Return Posting Cleanup")
public class SalesReturnPostingCleanupController {

    private static final String REF_TYPE = "SALES_RETURN";

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryService journalEntryService;
    private final SalesReturnRepository salesReturnRepository;
    private final TenantContext tenantContext;

    @PostMapping("/cleanup-duplicate-postings")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Cancel duplicate and premature sales-return credit notes (dry run unless apply=true)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanup(
            @RequestParam(defaultValue = "false") boolean apply) {

        UUID tenantId = tenantContext.current();

        List<JournalEntry> entries = journalEntryRepository
                .findByTenantIdAndReferenceTypeAndDeletedAtIsNullOrderByCreatedAtAsc(tenantId, REF_TYPE);

        // Group by the return each entry was raised for. Oldest first, so "keep the first" below
        // keeps the one raised when the return was first recorded.
        Map<UUID, List<JournalEntry>> byReturn = new LinkedHashMap<>();
        List<JournalEntry> unlinked = new ArrayList<>();
        for (JournalEntry je : entries) {
            if (je.getReferenceId() == null) unlinked.add(je);
            else byReturn.computeIfAbsent(je.getReferenceId(), k -> new ArrayList<>()).add(je);
        }

        Map<UUID, String> statusByReturn = new HashMap<>();
        for (SalesReturn r : salesReturnRepository.findByTenantIdAndDeletedAtIsNull(
                tenantId, org.springframework.data.domain.PageRequest.of(0, 20000))) {
            statusByReturn.put(r.getId(), r.getStatus());
        }

        List<Map<String, Object>> duplicates = new ArrayList<>();
        List<Map<String, Object>> premature = new ArrayList<>();

        for (Map.Entry<UUID, List<JournalEntry>> e : byReturn.entrySet()) {
            List<JournalEntry> group = e.getValue();
            String status = statusByReturn.get(e.getKey());

            // A return that was never approved should carry no credit note at all.
            if (status != null && !"APPROVED".equalsIgnoreCase(status) && !"CLOSED".equalsIgnoreCase(status)) {
                for (JournalEntry je : group) premature.add(describe(je, status));
                continue;
            }

            // Approved: keep the first, cancel the rest.
            for (int i = 1; i < group.size(); i++) duplicates.add(describe(group.get(i), status));
        }

        int cancelled = 0;
        if (apply) {
            for (Map<String, Object> row : concat(duplicates, premature)) {
                UUID id = (UUID) row.get("journalEntryId");
                try {
                    journalEntryService.cancel(id);
                    row.put("cancelled", true);
                    cancelled++;
                } catch (Exception ex) {
                    // One entry that will not cancel must not stop the rest.
                    row.put("cancelled", false);
                    row.put("error", ex.getMessage());
                    log.warn("Could not cancel journal entry {}: {}", id, ex.getMessage());
                }
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applied", apply);
        out.put("salesReturnEntriesExamined", entries.size());
        out.put("duplicateCount", duplicates.size());
        out.put("prematureCount", premature.size());
        out.put("cancelled", cancelled);
        out.put("duplicates", duplicates);
        out.put("premature", premature);
        out.put("unlinkedEntries", unlinked.size());
        return ResponseEntity.ok(ApiResponse.success(out,
                apply ? "Cleanup applied" : "Dry run — nothing was changed. Re-run with apply=true."));
    }

    private static List<Map<String, Object>> concat(List<Map<String, Object>> a, List<Map<String, Object>> b) {
        List<Map<String, Object>> all = new ArrayList<>(a);
        all.addAll(b);
        return all;
    }

    private static Map<String, Object> describe(JournalEntry je, String returnStatus) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("journalEntryId", je.getId());
        m.put("entryNumber", je.getEntryNumber());
        m.put("entryDate", je.getEntryDate() == null ? null : je.getEntryDate().toString());
        m.put("referenceNumber", je.getReferenceNumber());
        m.put("description", je.getDescription());
        m.put("status", je.getStatus() == null ? null : je.getStatus().name());
        m.put("salesReturnStatus", returnStatus);
        return m;
    }
}
