package com.erp.platform.modules.accounting;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.VoucherBook;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.repository.VoucherBookRepository;
import com.erp.platform.modules.accounting.service.VoucherBookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherBookService — routing postings to the right voucher book")
class VoucherBookServiceTest {

    @Mock private VoucherBookRepository voucherBookRepository;
    @Mock private JournalEntryRepository journalEntryRepository;
    @InjectMocks private VoucherBookService voucherBookService;

    private static final UUID TENANT_ID = TestDataBuilder.DEFAULT_TENANT_ID;

    @Test
    @DisplayName("a purchase invoice is numbered out of the Purchase Invoices book")
    void purchaseInvoiceUsesPurchaseBook() {
        VoucherBook pi = book("PI", "Purchase Invoices", "PURCHASE", 1);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "PI")).thenReturn(Optional.of(pi));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("PURCHASE_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherBookCode()).isEqualTo("PI");
        assertThat(je.getVoucherType()).isEqualTo("PURCHASE");
        assertThat(je.getEntryNumber()).isEqualTo("PI-00001");
        assertThat(pi.getCurrentNumber()).isEqualTo(2);   // series advanced
    }

    @Test
    @DisplayName("a sales invoice is numbered out of the Sales Invoices book")
    void salesInvoiceUsesSalesBook() {
        VoucherBook si = book("SI", "Sales Invoices", "SALES", 7);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "SI")).thenReturn(Optional.of(si));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("SALES_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherBookCode()).isEqualTo("SI");
        assertThat(je.getVoucherType()).isEqualTo("SALES");
        assertThat(je.getEntryNumber()).isEqualTo("SI-00007");
    }

    @Test
    @DisplayName("a Purchase Invoices book left typed JOURNAL is corrected, not treated as a journal")
    void mistypedPurchaseBookIsCorrected() {
        // What the old Voucher Book screen did to every book missing from its preset list.
        VoucherBook pi = book("PI", "Purchase Invoices", "JOURNAL", 1);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "PI")).thenReturn(Optional.of(pi));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("PURCHASE_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherType()).isEqualTo("PURCHASE");   // not JOURNAL
        assertThat(je.getVoucherBookCode()).isEqualTo("PI");
        assertThat(je.getEntryNumber()).isEqualTo("PI-00001");
        assertThat(pi.getVoucherType()).isEqualTo("PURCHASE");   // book itself repaired
    }

    @Test
    @DisplayName("an unmapped document falls back to the journal book")
    void unmappedDocumentUsesJournalBook() {
        VoucherBook je_ = book("JE", "Journal Entries", "JOURNAL", 3);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "JE")).thenReturn(Optional.of(je_));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("MANUAL_ADJUSTMENT");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherBookCode()).isEqualTo("JE");
        assertThat(je.getVoucherType()).isEqualTo("JOURNAL");
        assertThat(je.getEntryNumber()).isEqualTo("JE-00003");
    }

    @Test
    @DisplayName("the standard book is created when the tenant has none for that type")
    void createsStandardBookWhenMissing() {
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "PI")).thenReturn(Optional.empty());
        when(voucherBookRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(TENANT_ID)).thenReturn(List.of());
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("PURCHASE_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherBookCode()).isEqualTo("PI");
        assertThat(je.getVoucherBookName()).isEqualTo("Purchase Invoices");
        assertThat(je.getEntryNumber()).isEqualTo("PI-00001");
    }

    @Test
    @DisplayName("a renamed book of the same type is used instead of creating a duplicate")
    void fallsBackToTheTenantsOwnBookOfThatType() {
        VoucherBook renamed = book("GEN.PUR", "General Purchase", "PURCHASE", 12);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "PI")).thenReturn(Optional.empty());
        when(voucherBookRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(TENANT_ID))
                .thenReturn(List.of(renamed));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(eq(TENANT_ID), anyString()))
                .thenReturn(false);

        JournalEntry je = entry("PURCHASE_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getVoucherBookCode()).isEqualTo("GEN.PUR");
        assertThat(je.getEntryNumber()).isEqualTo("GEN.PUR-00012");
    }

    @Test
    @DisplayName("a number already in use is skipped rather than duplicated")
    void skipsNumbersAlreadyInUse() {
        VoucherBook si = book("SI", "Sales Invoices", "SALES", 1);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "SI")).thenReturn(Optional.of(si));
        when(voucherBookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(TENANT_ID, "SI-00001")).thenReturn(true);
        when(journalEntryRepository.existsByTenantIdAndEntryNumberAndDeletedAtIsNull(TENANT_ID, "SI-00002")).thenReturn(false);

        JournalEntry je = entry("SALES_INVOICE");
        voucherBookService.applyTo(je);

        assertThat(je.getEntryNumber()).isEqualTo("SI-00002");
        assertThat(si.getCurrentNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("a caller-supplied voucher number is kept, but the book is still recorded")
    void keepsCallerSuppliedNumber() {
        VoucherBook si = book("SI", "Sales Invoices", "SALES", 4);
        when(voucherBookRepository.findFirstByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullOrderByCreatedAtAsc(
                TENANT_ID, "SI")).thenReturn(Optional.of(si));

        JournalEntry je = entry("SALES_INVOICE");
        je.setEntryNumber("MIGRATED-42");
        voucherBookService.applyTo(je);

        assertThat(je.getEntryNumber()).isEqualTo("MIGRATED-42");
        assertThat(je.getVoucherBookCode()).isEqualTo("SI");
        assertThat(si.getCurrentNumber()).isEqualTo(4);   // series untouched
    }

    @Test
    @DisplayName("documentTypesFor() reports what a book takes, for the configuration screen")
    void documentTypesForReportsTheRouting() {
        assertThat(voucherBookService.documentTypesFor("PI")).containsExactly("PURCHASE_INVOICE");
        assertThat(voucherBookService.documentTypesFor("SI")).containsExactly("SALES_INVOICE", "INVOICE");
        assertThat(voucherBookService.documentTypesFor("XX")).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private JournalEntry entry(String referenceType) {
        JournalEntry je = new JournalEntry();
        je.setTenantId(TENANT_ID);
        je.setReferenceType(referenceType);
        return je;
    }

    private VoucherBook book(String code, String name, String type, int currentNumber) {
        VoucherBook vb = new VoucherBook();
        vb.setId(UUID.randomUUID());
        vb.setTenantId(TENANT_ID);
        vb.setCode(code);
        vb.setName(name);
        vb.setAbbreviation(code);
        vb.setVoucherType(type);
        vb.setStartNumber(1);
        vb.setCurrentNumber(currentNumber);
        vb.setActive(true);
        return vb;
    }
}
