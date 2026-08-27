package com.erp.platform.modules.sales;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.pricing.repository.PriceListItemRepository;
import com.erp.platform.modules.pricing.repository.PriceListRepository;
import com.erp.platform.modules.sales.dto.CreateSalesOrderRequest;
import com.erp.platform.modules.sales.dto.SalesOrderDto;
import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import com.erp.platform.modules.sales.service.SalesOrderService;
import com.erp.platform.modules.workflow.entity.WorkflowInstance.WorkflowStatus;
import com.erp.platform.modules.workflow.repository.WorkflowInstanceRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesOrderService unit tests")
class SalesOrderServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PriceListRepository priceListRepository;
    @Mock private PriceListItemRepository priceListItemRepository;
    @Mock private WorkflowInstanceRepository workflowInstanceRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private SalesOrderService salesOrderService;

    private static final UUID TENANT_ID   = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID ORDER_ID    = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without status returns all orders")
    void list_noFilter() {
        when(salesOrderRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        salesOrderService.list(null, PageRequest.of(0, 20));
        verify(salesOrderRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with status filter uses status query")
    void list_withStatus() {
        when(salesOrderRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(SalesOrderStatus.CONFIRMED), any()))
                .thenReturn(new PageImpl<>(List.of()));
        salesOrderService.list(SalesOrderStatus.CONFIRMED, PageRequest.of(0, 20));
        verify(salesOrderRepository).findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(SalesOrderStatus.CONFIRMED), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing order")
    void getById_found() {
        SalesOrder order = buildOrder(SalesOrderStatus.DRAFT);
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        SalesOrderDto dto = salesOrderService.getById(ORDER_ID);
        assertThat(dto.getId()).isEqualTo(ORDER_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing order")
    void getById_notFound() {
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> salesOrderService.getById(ORDER_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() sets DRAFT status and saves order")
    void create_success() {
        Customer customer = TestDataBuilder.customer().id(CUSTOMER_ID).build();
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
        when(priceListRepository.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(TENANT_ID))
                .thenReturn(Optional.empty());
        when(salesOrderRepository.save(any())).thenAnswer(i -> {
            SalesOrder saved = i.getArgument(0);
            saved.setId(ORDER_ID);
            return saved;
        });

        CreateSalesOrderRequest req = new CreateSalesOrderRequest();
        req.setCustomerId(CUSTOMER_ID);
        req.setOrderDate(LocalDate.now());

        SalesOrderDto dto = salesOrderService.create(req);
        assertThat(dto.getStatus()).isEqualTo(SalesOrderStatus.DRAFT);
        verify(salesOrderRepository).save(any());
    }

    @Test
    @DisplayName("create() throws NOT_FOUND when customer missing")
    void create_throwsCustomerNotFound() {
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.empty());
        CreateSalesOrderRequest req = new CreateSalesOrderRequest();
        req.setCustomerId(CUSTOMER_ID);
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> salesOrderService.create(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        verify(salesOrderRepository, never()).save(any());
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() transitions DRAFT to CONFIRMED when no workflow pending and credit ok")
    void updateStatus_draftToConfirmed() {
        SalesOrder order = buildOrder(SalesOrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.valueOf(500));
        Customer customer = TestDataBuilder.customer().id(CUSTOMER_ID).build();
        customer.setCreditLimit(BigDecimal.valueOf(10000));
        customer.setOutstandingBalance(BigDecimal.ZERO);

        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                TENANT_ID, ORDER_ID, "SALES_ORDER", WorkflowStatus.PENDING)).thenReturn(false);
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
        when(salesOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SalesOrderDto dto = salesOrderService.updateStatus(ORDER_ID, SalesOrderStatus.CONFIRMED);
        assertThat(dto.getStatus()).isEqualTo(SalesOrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus() throws when workflow approval is pending")
    void updateStatus_throwsWhenWorkflowPending() {
        SalesOrder order = buildOrder(SalesOrderStatus.DRAFT);
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                TENANT_ID, ORDER_ID, "SALES_ORDER", WorkflowStatus.PENDING)).thenReturn(true);

        Assertions.assertThrows(AppException.class,
                () -> salesOrderService.updateStatus(ORDER_ID, SalesOrderStatus.CONFIRMED));
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus() throws when credit limit is exceeded on CONFIRMED")
    void updateStatus_throwsOnCreditLimitExceeded() {
        SalesOrder order = buildOrder(SalesOrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.valueOf(9000));
        Customer customer = TestDataBuilder.customer().id(CUSTOMER_ID).build();
        customer.setCreditLimit(BigDecimal.valueOf(5000));
        customer.setOutstandingBalance(BigDecimal.valueOf(1000));

        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                any(), any(), any(), any())).thenReturn(false);
        when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        Assertions.assertThrows(AppException.class,
                () -> salesOrderService.updateStatus(ORDER_ID, SalesOrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("updateStatus() throws for invalid transition (SHIPPED to DRAFT)")
    void updateStatus_throwsForInvalidTransition() {
        SalesOrder order = buildOrder(SalesOrderStatus.SHIPPED);
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                any(), any(), any(), any())).thenReturn(false);

        Assertions.assertThrows(AppException.class,
                () -> salesOrderService.updateStatus(ORDER_ID, SalesOrderStatus.DRAFT));
    }

    @Test
    @DisplayName("updateStatus() allows CONFIRMED to PROCESSING")
    void updateStatus_confirmedToProcessing() {
        SalesOrder order = buildOrder(SalesOrderStatus.CONFIRMED);
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                any(), any(), any(), any())).thenReturn(false);
        when(salesOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SalesOrderDto dto = salesOrderService.updateStatus(ORDER_ID, SalesOrderStatus.PROCESSING);
        assertThat(dto.getStatus()).isEqualTo(SalesOrderStatus.PROCESSING);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes an order")
    void delete_setsDeletedAt() {
        SalesOrder order = buildOrder(SalesOrderStatus.DRAFT);
        when(salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ORDER_ID))
                .thenReturn(Optional.of(order));
        when(salesOrderRepository.save(any())).thenReturn(order);

        salesOrderService.delete(ORDER_ID);

        ArgumentCaptor<SalesOrder> captor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private SalesOrder buildOrder(SalesOrderStatus status) {
        SalesOrder o = new SalesOrder();
        o.setId(ORDER_ID);
        o.setTenantId(TENANT_ID);
        o.setOrderNumber("SO-2026-001");
        o.setCustomerId(CUSTOMER_ID);
        o.setCustomerName("Test Customer");
        o.setStatus(status);
        o.setOrderDate(LocalDate.now());
        o.setTotalAmount(BigDecimal.valueOf(1000));
        o.setDiscountAmount(BigDecimal.ZERO);
        o.setTaxAmount(BigDecimal.ZERO);
        o.setItems(new ArrayList<>());
        return o;
    }
}
