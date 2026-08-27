package com.erp.platform.modules.purchase;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.purchase.dto.CreateSupplierPaymentRequest;
import com.erp.platform.modules.purchase.dto.SupplierPaymentDto;
import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.entity.SupplierAdvance.AdvanceStatus;
import com.erp.platform.modules.purchase.entity.SupplierPayment;
import com.erp.platform.modules.purchase.entity.SupplierPayment.PayStatus;
import com.erp.platform.modules.purchase.repository.SupplierAdvanceRepository;
import com.erp.platform.modules.purchase.repository.SupplierPaymentRepository;
import com.erp.platform.modules.purchase.service.SupplierPaymentService;
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
@DisplayName("SupplierPaymentService unit tests")
class SupplierPaymentServiceTest {

    @Mock private SupplierPaymentRepository supplierPaymentRepository;
    @Mock private SupplierAdvanceRepository supplierAdvanceRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private JournalEntryService journalEntryService;
    @Mock private TenantContext tenantContext;
    @InjectMocks private SupplierPaymentService supplierPaymentService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final UUID VENDOR_ID  = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without filters returns all payments")
    void list_noFilter() {
        when(supplierPaymentRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        supplierPaymentService.list(null, null, PageRequest.of(0, 20));
        verify(supplierPaymentRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with vendorId filters by vendor")
    void list_byVendor() {
        when(supplierPaymentRepository.findByTenantIdAndVendorIdAndDeletedAtIsNull(eq(TENANT_ID), eq(VENDOR_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        supplierPaymentService.list(VENDOR_ID, null, PageRequest.of(0, 20));
        verify(supplierPaymentRepository).findByTenantIdAndVendorIdAndDeletedAtIsNull(eq(TENANT_ID), eq(VENDOR_ID), any());
    }

    @Test
    @DisplayName("list() with status filters by status")
    void list_byStatus() {
        when(supplierPaymentRepository.findByTenantIdAndStatusAndDeletedAtIsNull(eq(TENANT_ID), eq(PayStatus.DRAFT), any()))
                .thenReturn(new PageImpl<>(List.of()));
        supplierPaymentService.list(null, PayStatus.DRAFT, PageRequest.of(0, 20));
        verify(supplierPaymentRepository).findByTenantIdAndStatusAndDeletedAtIsNull(eq(TENANT_ID), eq(PayStatus.DRAFT), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing payment")
    void getById_found() {
        SupplierPayment pay = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(1000));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        SupplierPaymentDto dto = supplierPaymentService.getById(PAYMENT_ID);
        assertThat(dto.getId()).isEqualTo(PAYMENT_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing payment")
    void getById_notFound() {
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> supplierPaymentService.getById(PAYMENT_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() creates payment as DRAFT with correct net payment")
    void create_setsNetPayment() {
        CreateSupplierPaymentRequest req = new CreateSupplierPaymentRequest();
        req.setVendorId(VENDOR_ID);
        req.setVendorName("Test Vendor");
        req.setAmount(BigDecimal.valueOf(1000));
        req.setTdsAmount(BigDecimal.valueOf(100));
        req.setTdsPercent(BigDecimal.TEN);
        req.setPaymentDate(LocalDate.now());
        req.setPaymentMethod(SupplierPayment.PaymentMethod.BANK_TRANSFER);

        SupplierPayment saved = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(1000));
        saved.setNetPayment(BigDecimal.valueOf(900));
        when(supplierPaymentRepository.save(any())).thenReturn(saved);
        when(supplierPaymentRepository.countByTenantId(TENANT_ID)).thenReturn(0L);

        SupplierPaymentDto dto = supplierPaymentService.create(req);

        assertThat(dto.getStatus()).isEqualTo(PayStatus.DRAFT);
        verify(supplierPaymentRepository).save(any());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() throws when payment is not DRAFT")
    void update_throwsForNonDraft() {
        SupplierPayment pay = buildPayment(PayStatus.APPROVED, BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        Assertions.assertThrows(AppException.class,
                () -> supplierPaymentService.update(PAYMENT_ID, new CreateSupplierPaymentRequest()));
    }

    // ─── approve ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("approve() transitions PENDING_APPROVAL to APPROVED")
    void approve_success() {
        SupplierPayment pay = buildPayment(PayStatus.PENDING_APPROVAL, BigDecimal.valueOf(1000));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SupplierPaymentDto dto = supplierPaymentService.approve(PAYMENT_ID, "admin");
        assertThat(dto.getStatus()).isEqualTo(PayStatus.APPROVED);
    }

    @Test
    @DisplayName("approve() throws when status is not PENDING_APPROVAL")
    void approve_throwsForWrongStatus() {
        SupplierPayment pay = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(1000));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        Assertions.assertThrows(AppException.class,
                () -> supplierPaymentService.approve(PAYMENT_ID, "admin"));
    }

    // ─── process ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("process() transitions APPROVED to PROCESSED and posts GL journal")
    void process_postsGlJournal() {
        SupplierPayment pay = buildPayment(PayStatus.APPROVED, BigDecimal.valueOf(1000));
        pay.setNetPayment(BigDecimal.valueOf(1000));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(supplierAdvanceRepository.findByTenantIdAndVendorIdAndStatusNotAndDeletedAtIsNull(
                any(), any(), any())).thenReturn(List.of());
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "ACCOUNTS_PAYABLE"))
                .thenReturn(List.of(glAccount("2100")));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(TENANT_ID, "BANK"))
                .thenReturn(List.of(glAccount("1120")));

        SupplierPaymentDto dto = supplierPaymentService.process(PAYMENT_ID);

        assertThat(dto.getStatus()).isEqualTo(PayStatus.PROCESSED);
        verify(journalEntryService).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("process() skips GL when accounts not configured")
    void process_skipsGlWhenAccountsMissing() {
        SupplierPayment pay = buildPayment(PayStatus.APPROVED, BigDecimal.valueOf(500));
        pay.setNetPayment(BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(supplierAdvanceRepository.findByTenantIdAndVendorIdAndStatusNotAndDeletedAtIsNull(
                any(), any(), any())).thenReturn(List.of());
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(List.of());

        SupplierPaymentDto dto = supplierPaymentService.process(PAYMENT_ID);

        assertThat(dto.getStatus()).isEqualTo(PayStatus.PROCESSED);
        verify(journalEntryService, never()).create(any(JournalEntry.class));
    }

    @Test
    @DisplayName("process() applies available supplier advances before posting")
    void process_appliesAdvances() {
        SupplierPayment pay = buildPayment(PayStatus.APPROVED, BigDecimal.valueOf(1000));
        pay.setNetPayment(BigDecimal.valueOf(1000));
        pay.setVendorId(VENDOR_ID);

        SupplierAdvance advance = new SupplierAdvance();
        advance.setId(UUID.randomUUID());
        advance.setAmount(BigDecimal.valueOf(300));
        advance.setAdjustedAmount(BigDecimal.ZERO);
        advance.setStatus(AdvanceStatus.PENDING);

        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(supplierAdvanceRepository.findByTenantIdAndVendorIdAndStatusNotAndDeletedAtIsNull(
                eq(TENANT_ID), eq(VENDOR_ID), any())).thenReturn(List.of(advance));
        when(supplierAdvanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(any(), anyString()))
                .thenReturn(List.of());

        supplierPaymentService.process(PAYMENT_ID);

        verify(supplierAdvanceRepository).save(any());
    }

    @Test
    @DisplayName("process() throws when status is not APPROVED")
    void process_throwsForWrongStatus() {
        SupplierPayment pay = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        Assertions.assertThrows(AppException.class,
                () -> supplierPaymentService.process(PAYMENT_ID));
    }

    // ─── reconcile ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("reconcile() transitions PROCESSED to RECONCILED")
    void reconcile_success() {
        SupplierPayment pay = buildPayment(PayStatus.PROCESSED, BigDecimal.valueOf(1000));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        SupplierPaymentDto dto = supplierPaymentService.reconcile(PAYMENT_ID);
        assertThat(dto.getStatus()).isEqualTo(PayStatus.RECONCILED);
    }

    // ─── cancel ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancel() cancels a DRAFT payment")
    void cancel_fromDraft() {
        SupplierPayment pay = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        SupplierPaymentDto dto = supplierPaymentService.cancel(PAYMENT_ID);
        assertThat(dto.getStatus()).isEqualTo(PayStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() throws when payment is already PROCESSED")
    void cancel_throwsForProcessed() {
        SupplierPayment pay = buildPayment(PayStatus.PROCESSED, BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        Assertions.assertThrows(AppException.class, () -> supplierPaymentService.cancel(PAYMENT_ID));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes a DRAFT payment")
    void delete_softDeletes() {
        SupplierPayment pay = buildPayment(PayStatus.DRAFT, BigDecimal.valueOf(200));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        when(supplierPaymentRepository.save(any())).thenReturn(pay);

        supplierPaymentService.delete(PAYMENT_ID);

        ArgumentCaptor<SupplierPayment> captor = ArgumentCaptor.forClass(SupplierPayment.class);
        verify(supplierPaymentRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws when payment is not DRAFT")
    void delete_throwsForNonDraft() {
        SupplierPayment pay = buildPayment(PayStatus.APPROVED, BigDecimal.valueOf(500));
        when(supplierPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PAYMENT_ID))
                .thenReturn(Optional.of(pay));
        Assertions.assertThrows(AppException.class, () -> supplierPaymentService.delete(PAYMENT_ID));
        verify(supplierPaymentRepository, never()).save(any());
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private SupplierPayment buildPayment(PayStatus status, BigDecimal amount) {
        SupplierPayment p = new SupplierPayment();
        p.setId(PAYMENT_ID);
        p.setTenantId(TENANT_ID);
        p.setPaymentNumber("SPAY-2026-001");
        p.setVendorId(VENDOR_ID);
        p.setVendorName("Test Vendor");
        p.setAmount(amount);
        p.setNetPayment(amount);
        p.setTdsAmount(BigDecimal.ZERO);
        p.setTdsPercent(BigDecimal.ZERO);
        p.setStatus(status);
        p.setPaymentDate(LocalDate.now());
        p.setAllocations(new ArrayList<>());
        return p;
    }

    private Account glAccount(String code) {
        Account a = new Account();
        a.setId(UUID.randomUUID());
        a.setTenantId(TENANT_ID);
        a.setCode(code);
        a.setName("GL Account " + code);
        a.setType("ASSET");
        a.setBalance(BigDecimal.ZERO);
        return a;
    }
}
