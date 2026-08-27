package com.erp.platform.modules.accounting;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.service.AccountService;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.accounting.service.VoucherBookService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JournalEntryService unit tests")
class JournalEntryServiceTest {

    @Mock private JournalEntryRepository journalEntryRepository;
    @Mock private AccountService accountService;
    @Mock private VoucherBookService voucherBookService;
    @Mock private TenantContext tenantContext;
    @InjectMocks private JournalEntryService journalEntryService;

    private static final UUID TENANT_ID = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID JE_ID     = UUID.randomUUID();
    private static final UUID ACCT_DR   = UUID.randomUUID();
    private static final UUID ACCT_CR   = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without status returns all entries")
    void list_noFilter() {
        when(journalEntryRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        journalEntryService.list(null, PageRequest.of(0, 20));
        verify(journalEntryRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
        verify(journalEntryRepository, never()).findByTenantIdAndStatusAndDeletedAtIsNull(any(), any(), any());
    }

    @Test
    @DisplayName("list() with status filter uses status query")
    void list_withStatus() {
        when(journalEntryRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(JEStatus.POSTED), any()))
                .thenReturn(new PageImpl<>(List.of()));
        journalEntryService.list(JEStatus.POSTED, PageRequest.of(0, 20));
        verify(journalEntryRepository).findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(JEStatus.POSTED), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns entry when found")
    void getById_found() {
        JournalEntry je = balancedEntry();
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        JournalEntry result = journalEntryService.getById(JE_ID);
        assertThat(result.getId()).isEqualTo(JE_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND when missing")
    void getById_notFound() {
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> journalEntryService.getById(JE_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() sets tenantId, status DRAFT, calculates totals and numbers from the voucher book")
    void create_setsDefaultsAndTotals() {
        JournalEntry req = balancedEntry();
        req.setTenantId(null);
        req.setStatus(null);
        req.setEntryNumber(null);
        req.setReferenceType("SALES_INVOICE");
        doAnswer(i -> {
            JournalEntry e = i.getArgument(0);
            e.setVoucherBookCode("SI");
            e.setVoucherType("SALES");
            e.setEntryNumber("SI-00001");
            return null;
        }).when(voucherBookService).applyTo(any());
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.create(req);

        assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(result.getStatus()).isEqualTo(JEStatus.DRAFT);
        assertThat(result.getEntryNumber()).isEqualTo("SI-00001");
        assertThat(result.getVoucherBookCode()).isEqualTo("SI");
        assertThat(result.getVoucherType()).isEqualTo("SALES");
        assertThat(result.getTotalDebit()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(result.getTotalCredit()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("create() throws when entry is unbalanced")
    void create_throwsWhenUnbalanced() {
        JournalEntry req = unbalancedEntry();
        Assertions.assertThrows(AppException.class, () -> journalEntryService.create(req));
        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() sets back-reference on each line")
    void create_setsLineBackReference() {
        JournalEntry req = balancedEntry();
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.create(req);

        result.getLines().forEach(l -> assertThat(l.getJournalEntry()).isSameAs(result));
    }

    @Test
    @DisplayName("create() preserves existing entryNumber when supplied")
    void create_preservesEntryNumber() {
        JournalEntry req = balancedEntry();
        req.setEntryNumber("JE-CUSTOM-001");
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.create(req);
        assertThat(result.getEntryNumber()).isEqualTo("JE-CUSTOM-001");
    }

    // ─── post ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("post() transitions DRAFT entry to POSTED and adjusts account balances")
    void post_success() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.DRAFT);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountService.getById(ACCT_DR)).thenReturn(account(ACCT_DR, "ASSET"));
        when(accountService.getById(ACCT_CR)).thenReturn(account(ACCT_CR, "LIABILITY"));

        JournalEntry result = journalEntryService.post(JE_ID);

        assertThat(result.getStatus()).isEqualTo(JEStatus.POSTED);
        verify(accountService, times(2)).adjustBalance(any(), any());
    }

    @Test
    @DisplayName("post() throws when entry is not DRAFT")
    void post_throwsWhenNotDraft() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.POSTED);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        Assertions.assertThrows(AppException.class, () -> journalEntryService.post(JE_ID));
        verify(accountService, never()).adjustBalance(any(), any());
    }

    @Test
    @DisplayName("post() adjusts ASSET account balance by debit-minus-credit")
    void post_assetAccountBalanceIsDebitMinusCredit() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.DRAFT);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountService.getById(ACCT_DR)).thenReturn(account(ACCT_DR, "ASSET"));
        when(accountService.getById(ACCT_CR)).thenReturn(account(ACCT_CR, "LIABILITY"));

        journalEntryService.post(JE_ID);

        // DR line: ASSET account gets debit(500) - credit(0) = +500
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountService, atLeastOnce()).adjustBalance(eq(ACCT_DR), captor.capture());
        assertThat(captor.getAllValues().stream().filter(v -> v.compareTo(BigDecimal.ZERO) > 0)).isNotEmpty();
    }

    // ─── cancel ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel() transitions DRAFT to CANCELLED without reversing balances")
    void cancel_fromDraft() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.DRAFT);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        JournalEntry result = journalEntryService.cancel(JE_ID);

        assertThat(result.getStatus()).isEqualTo(JEStatus.CANCELLED);
        verify(accountService, never()).adjustBalance(any(), any());
    }

    @Test
    @DisplayName("cancel() from POSTED reverses account balances")
    void cancel_fromPosted_reversesBalances() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.POSTED);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        when(journalEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountService.getById(ACCT_DR)).thenReturn(account(ACCT_DR, "ASSET"));
        when(accountService.getById(ACCT_CR)).thenReturn(account(ACCT_CR, "LIABILITY"));

        JournalEntry result = journalEntryService.cancel(JE_ID);

        assertThat(result.getStatus()).isEqualTo(JEStatus.CANCELLED);
        verify(accountService, times(2)).adjustBalance(any(), any());
    }

    @Test
    @DisplayName("cancel() throws when already CANCELLED")
    void cancel_throwsWhenAlreadyCancelled() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.CANCELLED);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        Assertions.assertThrows(AppException.class, () -> journalEntryService.cancel(JE_ID));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes a DRAFT entry")
    void delete_softDeletesDraft() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.DRAFT);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        when(journalEntryRepository.save(any())).thenReturn(je);

        journalEntryService.delete(JE_ID);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws when entry is POSTED")
    void delete_throwsWhenPosted() {
        JournalEntry je = balancedEntry();
        je.setStatus(JEStatus.POSTED);
        when(journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, JE_ID))
                .thenReturn(Optional.of(je));
        Assertions.assertThrows(AppException.class, () -> journalEntryService.delete(JE_ID));
        verify(journalEntryRepository, never()).save(any());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private JournalEntry balancedEntry() {
        JournalEntry je = new JournalEntry();
        je.setId(JE_ID);
        je.setTenantId(TENANT_ID);
        je.setEntryNumber("JE-202601-TEST01");
        je.setStatus(JEStatus.DRAFT);
        je.setEntryDate(LocalDate.now());
        je.setDescription("Test entry");
        je.setTotalDebit(BigDecimal.valueOf(500));
        je.setTotalCredit(BigDecimal.valueOf(500));

        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccountId(ACCT_DR);
        dr.setDebitAmount(BigDecimal.valueOf(500));
        dr.setCreditAmount(BigDecimal.ZERO);

        JournalEntryLine cr = new JournalEntryLine();
        cr.setAccountId(ACCT_CR);
        cr.setDebitAmount(BigDecimal.ZERO);
        cr.setCreditAmount(BigDecimal.valueOf(500));

        je.setLines(new ArrayList<>(List.of(dr, cr)));
        return je;
    }

    private JournalEntry unbalancedEntry() {
        JournalEntry je = new JournalEntry();
        je.setId(JE_ID);
        je.setTenantId(TENANT_ID);
        je.setEntryNumber("JE-UNBAL");
        je.setTotalDebit(BigDecimal.ZERO);
        je.setTotalCredit(BigDecimal.ZERO);

        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccountId(ACCT_DR);
        dr.setDebitAmount(BigDecimal.valueOf(300));
        dr.setCreditAmount(BigDecimal.ZERO);

        je.setLines(new ArrayList<>(List.of(dr)));
        return je;
    }

    private Account account(UUID id, String type) {
        Account a = new Account();
        a.setId(id);
        a.setTenantId(TENANT_ID);
        a.setCode("1000");
        a.setName("Test Account");
        a.setType(type);
        a.setBalance(BigDecimal.ZERO);
        return a;
    }
}
