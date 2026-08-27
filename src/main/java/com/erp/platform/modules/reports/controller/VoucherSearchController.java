package com.erp.platform.modules.reports.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.BankVoucher;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.repository.BankVoucherRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Voucher Entry Search report (Accounting doc). Aggregates journal entries and
 * bank vouchers (payments + receipts) into one searchable list with the
 * documented filters: voucher type, location, ledger, group, party, status,
 * number, reference and date range. Backs /api/v1/reports/voucher-search,
 * which the Voucher Search screen calls.
 */
@RestController
@RequestMapping("/api/v1/reports/voucher-search")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VoucherSearchController {

    private final JournalEntryRepository journalEntryRepository;
    private final BankVoucherRepository bankVoucherRepository;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String partyName,
            @RequestParam(required = false) String narration,
            @RequestParam(required = false) String voucherNumber,
            @RequestParam(required = false) String referenceNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String ledger,
            @RequestParam(required = false) String group) {

        UUID tenantId = tenantContext.current();
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);

        List<Map<String, Object>> all = new ArrayList<>();

        // ── Journal entries ──
        for (JournalEntry j : journalEntryRepository
                .findByTenantIdAndDeletedAtIsNull(tenantId, Pageable.unpaged()).getContent()) {
            List<String> ledgers = j.getLines() == null ? List.of()
                    : j.getLines().stream().map(l -> l.getAccountName()).filter(Objects::nonNull).toList();
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", j.getId());
            // The book the entry was numbered out of decides its type — a sales invoice posting is a
            // SALES voucher, not a generic journal. Pre-book entries fall back to JOURNAL.
            r.put("voucherType", j.getVoucherType() == null || j.getVoucherType().isBlank()
                    ? "JOURNAL" : j.getVoucherType());
            r.put("voucherBookCode", nz(j.getVoucherBookCode()));
            r.put("voucherBookName", nz(j.getVoucherBookName()));
            r.put("voucherNumber", nz(j.getEntryNumber()));
            r.put("date", j.getEntryDate() != null ? j.getEntryDate().toString() : "");
            r.put("description", nz(j.getDescription()));
            r.put("narration", nz(j.getDescription()));
            r.put("referenceNumber", nz(j.getReferenceNumber()));
            r.put("referenceType", nz(j.getReferenceType()));
            r.put("amount", j.getTotalDebit());
            r.put("status", j.getStatus() != null ? j.getStatus().name() : "");
            r.put("partyName", "");
            r.put("bankAccountName", "");
            r.put("paymentMode", "");
            r.put("location", "");
            r.put("ledgers", ledgers);
            r.put("groups", List.<String>of());
            all.add(r);
        }

        // ── Bank vouchers (payments + receipts) ──
        for (BankVoucher b : bankVoucherRepository.findByTenantIdAndDeletedAtIsNull(tenantId)) {
            List<String> ledgers = b.getLines() == null ? List.of()
                    : b.getLines().stream().map(l -> l.getLedgerAccountName()).filter(Objects::nonNull).toList();
            List<String> groups = b.getLines() == null ? List.of()
                    : b.getLines().stream().map(l -> l.getAccountGroup()).filter(Objects::nonNull).toList();
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", b.getId());
            r.put("voucherType", b.getVoucherType() != null ? b.getVoucherType().name() : "");
            r.put("voucherBookCode", "");
            r.put("voucherBookName", "");
            r.put("voucherNumber", nz(b.getVoucherNumber()));
            r.put("date", b.getVoucherDate() != null ? b.getVoucherDate().toString() : "");
            r.put("description", nz(b.getPurpose()));
            r.put("narration", nz(b.getNarration()));
            r.put("referenceNumber", nz(b.getReferenceVoucherNumber()));
            r.put("referenceType", "");
            r.put("amount", b.getTotalAmount());
            r.put("status", nz(b.getStatus()));
            r.put("partyName", nz(b.getPartyName()));
            r.put("bankAccountName", nz(b.getBankAccountName()));
            r.put("paymentMode", nz(b.getPaymentMode()));
            r.put("location", nz(b.getLocationName()));
            r.put("ledgers", ledgers);
            r.put("groups", groups);
            all.add(r);
        }

        // ── Filters ──
        List<Map<String, Object>> filtered = all.stream()
                .filter(r -> voucherType == null || voucherType.isBlank() || voucherType.equalsIgnoreCase((String) r.get("voucherType")))
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase((String) r.get("status")))
                .filter(r -> withinDate((String) r.get("date"), fromDate, toDate))
                .filter(r -> contains((String) r.get("partyName"), partyName))
                .filter(r -> contains((String) r.get("narration"), narration))
                .filter(r -> contains((String) r.get("voucherNumber"), voucherNumber))
                .filter(r -> contains((String) r.get("referenceNumber"), referenceNo))
                .filter(r -> contains((String) r.get("location"), location))
                .filter(r -> listContains(r.get("ledgers"), ledger))
                .filter(r -> listContains(r.get("groups"), group))
                .sorted(Comparator.comparing((Map<String, Object> r) -> (String) r.get("date")).reversed())
                .collect(Collectors.toList());

        // strip internal helper keys before returning
        filtered.forEach(r -> { r.remove("ledgers"); r.remove("groups"); });

        int total = filtered.size();
        int fromIdx = Math.min(page * size, total);
        int toIdx = Math.min(fromIdx + size, total);
        List<Map<String, Object>> pageContent = filtered.subList(fromIdx, toIdx);
        Page<Map<String, Object>> result =
                new PageImpl<>(pageContent, PageRequest.of(page, size), total);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private static boolean contains(String value, String filter) {
        if (filter == null || filter.isBlank()) return true;
        return value != null && value.toLowerCase().contains(filter.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private static boolean listContains(Object list, String filter) {
        if (filter == null || filter.isBlank()) return true;
        if (!(list instanceof List)) return false;
        return ((List<String>) list).stream()
                .anyMatch(v -> v != null && v.toLowerCase().contains(filter.toLowerCase()));
    }

    private static boolean withinDate(String dateStr, LocalDate from, LocalDate to) {
        if ((from == null && to == null) || dateStr == null || dateStr.isBlank()) return true;
        LocalDate d = parseDate(dateStr);
        if (d == null) return true;
        if (from != null && d.isBefore(from)) return false;
        if (to != null && d.isAfter(to)) return false;
        return true;
    }
}
