package com.erp.platform.modules.sales;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.sales.dto.CreateInvoiceRequest;
import com.erp.platform.modules.sales.dto.InvoiceDto;
import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.service.InvoiceService;
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
@DisplayName("InvoiceService unit tests")
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private InvoiceService invoiceService;

    private static final UUID TENANT_ID   = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID INVOICE_ID  = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    // ─── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() returns all invoices when no status filter")
    void list_withoutStatusFilter() {
        when(invoiceRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        invoiceService.list(null, PageRequest.of(0, 20));
        verify(invoiceRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
        verify(invoiceRepository, never())
                .findByTenantIdAndStatusAndDeletedAtIsNull(any(), any(), any());
    }

    @Test
    @DisplayName("list() filters by status when provided")
    void list_withStatusFilter() {
        when(invoiceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(InvoiceStatus.PAID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        invoiceService.list(InvoiceStatus.PAID, PageRequest.of(0, 20));
        verify(invoiceRepository).findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(InvoiceStatus.PAID), any());
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing invoice")
    void getById_success() {
        Invoice inv = buildInvoice(InvoiceStatus.DRAFT, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));

        InvoiceDto dto = invoiceService.getById(INVOICE_ID);
        assertThat(dto.getId()).isEqualTo(INVOICE_ID);
        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing invoice")
    void getById_notFound() {
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> invoiceService.getById(INVOICE_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() produces invoice with correct total amount")
    void create_calculatesCorrectTotal() {
        Customer customer = TestDataBuilder.customer().id(CUSTOMER_ID).build();
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        CreateInvoiceRequest.CreateInvoiceItemRequest item = new CreateInvoiceRequest.CreateInvoiceItemRequest();
        item.setProductName("Product A");
        item.setQuantity(BigDecimal.valueOf(2));
        item.setUnitPrice(BigDecimal.valueOf(100));
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setTaxPercent(BigDecimal.valueOf(18));

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(List.of(item));

        Invoice savedInvoice = buildInvoice(InvoiceStatus.DRAFT,
                BigDecimal.valueOf(236), BigDecimal.ZERO);
        when(invoiceRepository.save(any())).thenReturn(savedInvoice);

        InvoiceDto dto = invoiceService.create(request);
        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        verify(invoiceRepository).save(any());
    }

    @Test
    @DisplayName("create() throws NOT_FOUND when customer doesn't exist")
    void create_throwsWhenCustomerNotFound() {
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.empty());

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setCustomerId(CUSTOMER_ID);

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> invoiceService.create(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        verify(invoiceRepository, never()).save(any());
    }

    // ─── recordPayment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("recordPayment() marks invoice as PAID when full amount received")
    void recordPayment_fullPaymentMarksPaid() {
        Invoice inv = buildInvoice(InvoiceStatus.SENT, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InvoiceDto dto = invoiceService.recordPayment(INVOICE_ID, BigDecimal.valueOf(1000), "BANK", "REF001");

        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(dto.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(dto.getBalanceDue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("recordPayment() marks invoice as PARTIALLY_PAID for partial amount")
    void recordPayment_partialPaymentMarksPartial() {
        Invoice inv = buildInvoice(InvoiceStatus.SENT, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InvoiceDto dto = invoiceService.recordPayment(INVOICE_ID, BigDecimal.valueOf(400), "CASH", "REF002");

        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(dto.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(dto.getBalanceDue()).isEqualByComparingTo(BigDecimal.valueOf(600));
    }

    @Test
    @DisplayName("recordPayment() throws when payment exceeds invoice total")
    void recordPayment_throwsWhenExceedsTotal() {
        Invoice inv = buildInvoice(InvoiceStatus.SENT, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));

        Assertions.assertThrows(AppException.class,
                () -> invoiceService.recordPayment(INVOICE_ID, BigDecimal.valueOf(1500), "BANK", "REF003"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("recordPayment() throws when invoice is CANCELLED")
    void recordPayment_throwsOnCancelledInvoice() {
        Invoice inv = buildInvoice(InvoiceStatus.CANCELLED, BigDecimal.valueOf(1000), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));

        Assertions.assertThrows(AppException.class,
                () -> invoiceService.recordPayment(INVOICE_ID, BigDecimal.valueOf(500), "BANK", "REF004"));
    }

    @Test
    @DisplayName("recordPayment() accumulates multiple partial payments")
    void recordPayment_accumulatesPartialPayments() {
        Invoice inv = buildInvoice(InvoiceStatus.SENT, BigDecimal.valueOf(1000), BigDecimal.valueOf(300));
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InvoiceDto dto = invoiceService.recordPayment(INVOICE_ID, BigDecimal.valueOf(700), "UPI", "REF005");

        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(dto.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() transitions invoice to new status")
    void updateStatus_transitionsStatus() {
        Invoice inv = buildInvoice(InvoiceStatus.DRAFT, BigDecimal.valueOf(500), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InvoiceDto dto = invoiceService.updateStatus(INVOICE_ID, InvoiceStatus.SENT);
        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.SENT);
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes invoice")
    void delete_setsDeletedAt() {
        Invoice inv = buildInvoice(InvoiceStatus.DRAFT, BigDecimal.valueOf(100), BigDecimal.ZERO);
        when(invoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, INVOICE_ID))
                .thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any())).thenReturn(inv);

        invoiceService.delete(INVOICE_ID);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Invoice buildInvoice(InvoiceStatus status, BigDecimal totalAmount, BigDecimal paidAmount) {
        Invoice inv = new Invoice();
        inv.setId(INVOICE_ID);
        inv.setTenantId(TENANT_ID);
        inv.setInvoiceNumber("INV-202605-XXXXXX");
        inv.setCustomerId(CUSTOMER_ID);
        inv.setCustomerName("Test Customer");
        inv.setStatus(status);
        inv.setTotalAmount(totalAmount);
        inv.setPaidAmount(paidAmount);
        inv.setBalanceDue(totalAmount.subtract(paidAmount));
        inv.setInvoiceDate(LocalDate.now());
        inv.setDueDate(LocalDate.now().plusDays(30));
        inv.setSubtotal(totalAmount);
        inv.setTaxAmount(BigDecimal.ZERO);
        inv.setDiscountAmount(BigDecimal.ZERO);
        inv.setItems(new ArrayList<>());
        return inv;
    }
}
