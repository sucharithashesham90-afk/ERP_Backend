package com.erp.platform.modules.accounting.service;

import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.VoucherBook;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.repository.VoucherBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Routes every posting into the voucher book its document type belongs to — a purchase invoice into
 * the Purchase Invoices book, a sales invoice into the Sales Invoices book — and numbers the voucher
 * from that book's own series.
 *
 * <p>Before this, voucher books were pure configuration: nothing read them, and every journal entry
 * got a generic {@code JE-yyyyMM-xxxxxx} number and showed up in Voucher Search as type JOURNAL, so
 * the Purchase / Sales books never filled up.
 *
 * <p>The link between a document and its book is {@link JournalEntry#getReferenceType()} → the book
 * code in {@link #BOOK_BY_DOCUMENT_TYPE}. Resolution deliberately prefers what the tenant configured:
 * the book carrying the mapped code wins; if that code was renamed away, a single active book of the
 * same voucher type is used instead; only when neither exists is the standard book created.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherBookService {

    /** A voucher book the platform knows how to create on demand. */
    public record BookSpec(String code, String name, String voucherType) {}

    public static final BookSpec JOURNAL_BOOK = new BookSpec("JE", "Journal Entries", "JOURNAL");

    /**
     * Which book each posting source lands in. Keys are {@code JournalEntry.referenceType} values;
     * anything not listed falls back to the journal book.
     */
    private static final Map<String, BookSpec> BOOK_BY_DOCUMENT_TYPE = new LinkedHashMap<>();
    static {
        BookSpec salesInvoice     = new BookSpec("SI",  "Sales Invoices",     "SALES");
        BookSpec salesReturn      = new BookSpec("SR",  "Sales Returns",      "SALES");
        BookSpec creditNote       = new BookSpec("CN",  "Credit Notes",       "SALES");
        BookSpec purchaseInvoice  = new BookSpec("PI",  "Purchase Invoices",  "PURCHASE");
        BookSpec purchaseReturn   = new BookSpec("PR",  "Purchase Returns",   "PURCHASE");
        // The credit-note counterpart on the buying side: what we bill back to a supplier.
        BookSpec debitNote        = new BookSpec("DN",  "Debit Notes",        "PURCHASE");
        BookSpec goodsReceipt     = new BookSpec("GRN", "Goods Receipts",     "PURCHASE");
        BookSpec bankPayment      = new BookSpec("BP",  "Bank Payments",      "PAYMENT");
        BookSpec bankReceipt      = new BookSpec("BR",  "Bank Receipts",      "RECEIPT");

        // Sales side
        BOOK_BY_DOCUMENT_TYPE.put("SALES_INVOICE",           salesInvoice);
        BOOK_BY_DOCUMENT_TYPE.put("INVOICE",                 salesInvoice);
        BOOK_BY_DOCUMENT_TYPE.put("SALES_RETURN",            salesReturn);
        BOOK_BY_DOCUMENT_TYPE.put("CREDIT_NOTE",             creditNote);
        // Purchase side
        BOOK_BY_DOCUMENT_TYPE.put("PURCHASE_INVOICE",        purchaseInvoice);
        BOOK_BY_DOCUMENT_TYPE.put("PURCHASE_RETURN",         purchaseReturn);
        BOOK_BY_DOCUMENT_TYPE.put("DEBIT_NOTE",              debitNote);
        BOOK_BY_DOCUMENT_TYPE.put("GRN",                     goodsReceipt);
        // Money out
        BOOK_BY_DOCUMENT_TYPE.put("PAYMENT",                 bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("PAYMENT_REVERSAL",        bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("SUPPLIER_PAYMENT",        bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("SUPPLIER_ADVANCE",        bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("CUSTOMER_ADVANCE_REFUND", bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("PRODUCTION_JOB_ADVANCE",  bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("LIABILITY_PAYMENT",       bankPayment);
        BOOK_BY_DOCUMENT_TYPE.put("GROWER_ADVANCE",         bankPayment);
        // Money in
        BOOK_BY_DOCUMENT_TYPE.put("CUSTOMER_ADVANCE",        bankReceipt);
    }

    private final VoucherBookRepository voucherBookRepository;
    private final JournalEntryRepository journalEntryRepository;

    // ── Posting ──────────────────────────────────────────────────────────────

    /**
     * Stamps the entry with the book its document type belongs to and, unless the caller already
     * supplied one, numbers it from that book's series. Never throws: a configuration problem must
     * not stop an invoice from posting, so the entry falls back to the generic journal number.
     */
    @Transactional
    public void applyTo(JournalEntry entry) {
        if (entry == null) return;
        UUID tenantId = entry.getTenantId();
        if (tenantId != null) {
            try {
                VoucherBook book = resolveForDocumentType(tenantId, entry.getReferenceType());
                entry.setVoucherBookId(book.getId());
                entry.setVoucherBookCode(book.getCode());
                entry.setVoucherBookName(book.getName());
                entry.setVoucherType(isBlank(book.getVoucherType()) ? JOURNAL_BOOK.voucherType() : book.getVoucherType());
                if (isBlank(entry.getEntryNumber())) entry.setEntryNumber(issueNumber(tenantId, book));
            } catch (Exception e) {
                log.warn("Could not resolve a voucher book for referenceType={} — falling back to the generic series: {}",
                        entry.getReferenceType(), e.getMessage());
            }
        }
        if (isBlank(entry.getEntryNumber())) entry.setEntryNumber(fallbackNumber());
        if (isBlank(entry.getVoucherType())) entry.setVoucherType(JOURNAL_BOOK.voucherType());
    }

    /** The book a document type posts into, created from the standard definition if absent. */
    @Transactional
    public VoucherBook resolveForDocumentType(UUID tenantId, String documentType) {
        BookSpec spec = specFor(documentType);

        Optional<VoucherBook> byCode = voucherBookRepository
                .findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(tenantId, spec.code());
        if (byCode.isPresent() && byCode.get().isActive()) return retype(byCode.get(), spec);

        // The tenant renamed or replaced the standard book: fall back to what they configured for
        // this voucher type, as long as it is unambiguous.
        List<VoucherBook> sameType = voucherBookRepository
                .findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId).stream()
                .filter(b -> spec.voucherType().equalsIgnoreCase(b.getVoucherType()))
                .toList();
        for (VoucherBook b : sameType) {
            if (spec.code().equalsIgnoreCase(b.getCode()) || spec.code().equalsIgnoreCase(b.getAbbreviation())) return b;
        }
        if (sameType.size() == 1) return sameType.get(0);

        // An inactive book still owns the code in the unique (tenant_id, code) index, so reuse it
        // rather than fail creating a clashing one.
        if (byCode.isPresent()) return retype(byCode.get(), spec);

        return createStandardBook(tenantId, spec);
    }

    /**
     * Repairs a standard book that is filed under the wrong voucher type.
     *
     * <p>The Voucher Book screen used to force the type of any book missing from its preset list to
     * JOURNAL on save — and the two books period generation creates, Purchase Invoices (PI) and
     * Sales Invoices (SI), are both missing from that list. So on any tenant where someone opened
     * that screen and pressed Update, the purchase book is sitting there typed JOURNAL, and every
     * purchase invoice posted into it is stamped and reported as a journal voucher.
     *
     * <p>Only books matched by their standard code are corrected, and only to the type that code is
     * defined to mean, so a book the tenant deliberately typed differently under its own code is
     * left alone.
     */
    private VoucherBook retype(VoucherBook book, BookSpec spec) {
        if (spec.voucherType().equalsIgnoreCase(book.getVoucherType())) return book;
        log.info("Voucher book {} was typed {} — correcting to {} so {} postings are filed under it",
                book.getCode(), book.getVoucherType(), spec.voucherType(), spec.voucherType());
        book.setVoucherType(spec.voucherType());
        return voucherBookRepository.save(book);
    }

    /** Next number in the book's series, e.g. {@code PI-00007}, and advances the series. */
    @Transactional
    public String issueNumber(UUID tenantId, VoucherBook book) {
        String base = firstNonBlank(book.getPrefix(), book.getAbbreviation(), book.getCode(), JOURNAL_BOOK.code());
        String suffix = book.getSuffix() == null ? "" : book.getSuffix().trim();
        int start = book.getStartNumber() == null ? 1 : Math.max(1, book.getStartNumber());
        int next = book.getCurrentNumber() == null ? start : Math.max(start, book.getCurrentNumber());

        String number = format(base, next, suffix);
        for (int guard = 0; guard < 10_000
                && journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(tenantId, number); guard++) {
            next++;
            number = format(base, next, suffix);
        }

        book.setCurrentNumber(next + 1);
        voucherBookRepository.save(book);
        return number;
    }

    // ── Configuration views ──────────────────────────────────────────────────

    /** Document types routed into a given book code — shown on the voucher book screens. */
    public List<String> documentTypesFor(String bookCode) {
        List<String> types = new ArrayList<>();
        if (bookCode == null) return types;
        BOOK_BY_DOCUMENT_TYPE.forEach((docType, spec) -> {
            if (spec.code().equalsIgnoreCase(bookCode)) types.add(docType);
        });
        return types;
    }

    /** The full document-type → book routing, for the accounting configuration screen. */
    public List<Map<String, Object>> documentMapping() {
        List<Map<String, Object>> rows = new ArrayList<>();
        BOOK_BY_DOCUMENT_TYPE.forEach((docType, spec) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("documentType", docType);
            row.put("label", label(docType));
            row.put("bookCode", spec.code());
            row.put("bookName", spec.name());
            row.put("voucherType", spec.voucherType());
            rows.add(row);
        });
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("documentType", "*");
        fallback.put("label", "Everything else");
        fallback.put("bookCode", JOURNAL_BOOK.code());
        fallback.put("bookName", JOURNAL_BOOK.name());
        fallback.put("voucherType", JOURNAL_BOOK.voucherType());
        rows.add(fallback);
        return rows;
    }

    /**
     * Classifies journal entries posted before books were wired into posting. Only the book and
     * voucher type are stamped — numbers already issued are audit trail and are left untouched.
     */
    @Transactional
    public Map<String, Object> backfillJournalEntries(UUID tenantId) {
        int stamped = 0, unchanged = 0, failed = 0;
        List<JournalEntry> entries = journalEntryRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        for (JournalEntry je : entries) {
            try {
                VoucherBook book = resolveForDocumentType(tenantId, je.getReferenceType());
                String type = isBlank(book.getVoucherType()) ? JOURNAL_BOOK.voucherType() : book.getVoucherType();
                // Re-stamp when the entry disagrees with the book it belongs to, not only when it is
                // unclassified — entries filed while a book was typed wrong need correcting too.
                if (book.getId().equals(je.getVoucherBookId()) && type.equals(je.getVoucherType())) {
                    unchanged++;
                    continue;
                }
                je.setVoucherBookId(book.getId());
                je.setVoucherBookCode(book.getCode());
                je.setVoucherBookName(book.getName());
                je.setVoucherType(type);
                journalEntryRepository.save(je);
                stamped++;
            } catch (Exception e) {
                failed++;
                log.warn("Voucher book backfill: could not classify entry {}: {}", je.getEntryNumber(), e.getMessage());
            }
        }
        log.info("Voucher book backfill: {} entries, {} stamped, {} already classified, {} failed",
                entries.size(), stamped, unchanged, failed);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", entries.size());
        result.put("stamped", stamped);
        result.put("unchanged", unchanged);
        result.put("failed", failed);
        return result;
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private VoucherBook createStandardBook(UUID tenantId, BookSpec spec) {
        VoucherBook vb = new VoucherBook();
        vb.setTenantId(tenantId);
        vb.setCode(spec.code());
        vb.setName(spec.name());
        vb.setAbbreviation(spec.code());
        vb.setVoucherType(spec.voucherType());
        vb.setPrefix(spec.code());
        vb.setStartNumber(1);
        vb.setCurrentNumber(1);
        vb.setActive(true);
        vb.setAutoPosting(true);
        vb.setAutoPostToLedger(true);
        log.info("Created standard voucher book {} ({}) for tenant {}", spec.code(), spec.voucherType(), tenantId);
        return voucherBookRepository.save(vb);
    }

    private BookSpec specFor(String documentType) {
        if (isBlank(documentType)) return JOURNAL_BOOK;
        return BOOK_BY_DOCUMENT_TYPE.getOrDefault(documentType.trim().toUpperCase(Locale.ROOT), JOURNAL_BOOK);
    }

    private static String format(String base, int number, String suffix) {
        return base + "-" + String.format("%05d", number) + suffix;
    }

    private static String label(String documentType) {
        String[] words = documentType.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(w.charAt(0)).append(w.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (!isBlank(v)) return v.trim();
        return JOURNAL_BOOK.code();
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    /** Only used when no book could be resolved at all — keeps a posting from failing outright. */
    private static String fallbackNumber() {
        return "JE-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
