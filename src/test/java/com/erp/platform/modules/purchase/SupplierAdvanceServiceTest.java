package com.erp.platform.modules.purchase;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.purchase.entity.AdvanceAdjustment;
import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.entity.SupplierAdvance.AdvanceStatus;
import com.erp.platform.modules.purchase.repository.AdvanceAdjustmentRepository;
import com.erp.platform.modules.purchase.repository.SupplierAdvanceRepository;
import com.erp.platform.modules.purchase.service.SupplierAdvanceService;
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
@DisplayName("SupplierAdvanceService unit tests")
class SupplierAdvanceServiceTest {

    @Mock private SupplierAdvanceRepository repo;
    @Mock private AdvanceAdjustmentRepository adjustmentRepo;
    @Mock private AccountRepository accountRepository;
    @Mock private JournalEntryService journalEntryService;
    @Mock private TenantContext tenantContext;
    @InjectMocks private SupplierAdvanceService supplierAdvanceService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID ADVANCE_ID = UUID.randomUUID();
    private static final UUID VENDOR_ID  = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without vendorId returns all advances")
    void list_noFilter() {
        when(repo.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        supplierAdvanceService.list(null, PageRequest.of(0, 20));
        verify(repo).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with vendorId filters by vendor")
    void list_byVendor() {
        when(repo.findByTenantIdAndVendorIdAndDeletedAtIsNull(eq(TENANT_ID), eq(VENDOR_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        supplierAdvanceService.list(VENDOR_ID, PageRequest.of(0, 20));
        verify(repo).findByTenantIdAndVendorIdAndDeletedAtIsNull(eq(TENANT_ID), eq(VENDOR_ID), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns advance for existing id")
    void getById_found() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        SupplierAdvance result = supplierAdvanceService.getById(ADVANCE_ID);
        assertThat(result.getId()).isEqualTo(ADVANCE_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND when advance missing")
    void getById_notFound() {
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> supplierAdvanceService.getById(ADVANCE_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() sets PENDING status and zero adjustedAmount")
    void create_setsPendingAndZeroAdjusted() {
        SupplierAdvance req = buildAdvance(null, BigDecimal.valueOf(5000));
        req.setStatus(null);
        when(repo.save(any())).thenAnswer(i -> {
            SupplierAdvance saved = i.getArgument(0);
            saved.setId(ADVANCE_ID);
            return saved;
        });
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(List.of());

        SupplierAdvance result = supplierAdvanceService.create(req);

        assertThat(result.getStatus()).isEqualTo(AdvanceStatus.PENDING);
        assertThat(result.getAdjustedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("create() posts GL journal when accounts are configured")
    void create_postsGlJournal() {
        SupplierAdvance req = buildAdvance(null, BigDecimal.valueOf(5000));
        when(repo.save(any())).thenAnswer(i -> {
            SupplierAdvance saved = i.getArgument(0);
            saved.setId(ADVANCE_ID);
            return saved;
        });
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "ADVANCE_TO_VENDOR"))
                .thenReturn(List.of(glAccount("1501")));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "BANK"))
                .thenReturn(List.of(glAccount("1120")));

        supplierAdvanceService.create(req);

        verify(journalEntryService).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("create() skips GL when accounts not configured")
    void create_skipsGlWhenAccountsMissing() {
        SupplierAdvance req = buildAdvance(null, BigDecimal.valueOf(5000));
        when(repo.save(any())).thenAnswer(i -> {
            SupplierAdvance saved = i.getArgument(0);
            saved.setId(ADVANCE_ID);
            return saved;
        });
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(List.of());

        supplierAdvanceService.create(req);

        verify(journalEntryService, never()).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("create() skips GL when amount is zero")
    void create_skipsGlForZeroAmount() {
        SupplierAdvance req = buildAdvance(null, BigDecimal.ZERO);
        when(repo.save(any())).thenAnswer(i -> {
            SupplierAdvance saved = i.getArgument(0);
            saved.setId(ADVANCE_ID);
            return saved;
        });

        supplierAdvanceService.create(req);

        verify(journalEntryService, never()).create(any(JournalEntry.class));
        verify(accountRepository, never()).findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() saves updated fields for PENDING advance")
    void update_pendingSuccess() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        SupplierAdvance body = buildAdvance(null, BigDecimal.valueOf(7000));
        body.setDescription("Updated advance");
        SupplierAdvance result = supplierAdvanceService.update(ADVANCE_ID, body);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(7000));
    }

    @Test
    @DisplayName("update() throws when advance is not PENDING")
    void update_throwsForNonPending() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.FULLY_ADJUSTED, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        Assertions.assertThrows(AppException.class,
                () -> supplierAdvanceService.update(ADVANCE_ID, new SupplierAdvance()));
    }

    // ─── cancel ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel() cancels a PENDING advance")
    void cancel_fromPending() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        SupplierAdvance result = supplierAdvanceService.cancel(ADVANCE_ID);
        assertThat(result.getStatus()).isEqualTo(AdvanceStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() throws for FULLY_ADJUSTED advance")
    void cancel_throwsForFullyAdjusted() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.FULLY_ADJUSTED, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        Assertions.assertThrows(AppException.class, () -> supplierAdvanceService.cancel(ADVANCE_ID));
    }

    @Test
    @DisplayName("cancel() throws for already CANCELLED advance")
    void cancel_throwsForAlreadyCancelled() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.CANCELLED, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        Assertions.assertThrows(AppException.class, () -> supplierAdvanceService.cancel(ADVANCE_ID));
    }

    // ─── adjust ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("adjust() partially adjusts advance and sets PARTIALLY_ADJUSTED status")
    void adjust_partial() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(5000));
        adv.setAdjustedAmount(BigDecimal.ZERO);
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        AdvanceAdjustment adj = new AdvanceAdjustment();
        adj.setAdjustedAmount(BigDecimal.valueOf(2000));

        SupplierAdvance result = supplierAdvanceService.adjust(ADVANCE_ID, adj);
        assertThat(result.getStatus()).isEqualTo(AdvanceStatus.PARTIALLY_ADJUSTED);
        assertThat(result.getAdjustedAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("adjust() fully adjusts advance and sets FULLY_ADJUSTED status")
    void adjust_full() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(5000));
        adv.setAdjustedAmount(BigDecimal.ZERO);
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        AdvanceAdjustment adj = new AdvanceAdjustment();
        adj.setAdjustedAmount(BigDecimal.valueOf(5000));

        SupplierAdvance result = supplierAdvanceService.adjust(ADVANCE_ID, adj);
        assertThat(result.getStatus()).isEqualTo(AdvanceStatus.FULLY_ADJUSTED);
    }

    @Test
    @DisplayName("adjust() throws when adjustment exceeds available balance")
    void adjust_throwsWhenExceedsBalance() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.PENDING, BigDecimal.valueOf(1000));
        adv.setAdjustedAmount(BigDecimal.ZERO);
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));

        AdvanceAdjustment adj = new AdvanceAdjustment();
        adj.setAdjustedAmount(BigDecimal.valueOf(2000));

        Assertions.assertThrows(AppException.class,
                () -> supplierAdvanceService.adjust(ADVANCE_ID, adj));
        verify(adjustmentRepo, never()).save(any());
    }

    @Test
    @DisplayName("adjust() throws for CANCELLED advance")
    void adjust_throwsForCancelled() {
        SupplierAdvance adv = buildAdvance(AdvanceStatus.CANCELLED, BigDecimal.valueOf(5000));
        when(repo.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ADVANCE_ID))
                .thenReturn(Optional.of(adv));
        AdvanceAdjustment adj = new AdvanceAdjustment();
        adj.setAdjustedAmount(BigDecimal.valueOf(100));
        Assertions.assertThrows(AppException.class,
                () -> supplierAdvanceService.adjust(ADVANCE_ID, adj));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private SupplierAdvance buildAdvance(AdvanceStatus status, BigDecimal amount) {
        SupplierAdvance a = new SupplierAdvance();
        a.setId(ADVANCE_ID);
        a.setTenantId(TENANT_ID);
        com.erp.platform.modules.master.entity.Vendor v = new com.erp.platform.modules.master.entity.Vendor();
        v.setId(VENDOR_ID);
        a.setVendor(v);
        a.setAmount(amount);
        a.setAdjustedAmount(BigDecimal.ZERO);
        a.setStatus(status != null ? status : AdvanceStatus.PENDING);
        a.setAdvanceDate(LocalDate.now());
        return a;
    }

    private Account glAccount(String code) {
        Account a = new Account();
        a.setId(UUID.randomUUID());
        a.setTenantId(TENANT_ID);
        a.setCode(code);
        a.setName("GL " + code);
        a.setBalance(BigDecimal.ZERO);
        return a;
    }
}
