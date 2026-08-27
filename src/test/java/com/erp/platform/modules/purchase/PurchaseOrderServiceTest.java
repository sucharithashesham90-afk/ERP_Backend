package com.erp.platform.modules.purchase;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.dto.CreatePurchaseOrderRequest;
import com.erp.platform.modules.purchase.dto.PurchaseOrderDto;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import com.erp.platform.modules.purchase.service.PurchaseOrderService;
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
@DisplayName("PurchaseOrderService unit tests")
class PurchaseOrderServiceTest {

    @Mock private PurchaseOrderRepository poRepository;
    @Mock private GoodsReceiptRepository grnRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private PurchaseOrderService purchaseOrderService;

    private static final UUID TENANT_ID = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID PO_ID     = UUID.randomUUID();
    private static final UUID VENDOR_ID = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    // ─── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without status returns all POs")
    void list_withoutStatusFilter() {
        when(poRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        purchaseOrderService.list(null, null, PageRequest.of(0, 20));
        verify(poRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with status filters by that status")
    void list_withStatusFilter() {
        when(poRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(POStatus.CONFIRMED), any()))
                .thenReturn(new PageImpl<>(List.of()));
        purchaseOrderService.list(null, POStatus.CONFIRMED, PageRequest.of(0, 20));
        verify(poRepository).findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(POStatus.CONFIRMED), any());
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing PO")
    void getById_success() {
        PurchaseOrder po = purchaseOrder(POStatus.DRAFT);
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.of(po));

        PurchaseOrderDto dto = purchaseOrderService.getById(PO_ID);
        assertThat(dto.getId()).isEqualTo(PO_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing PO")
    void getById_notFound() {
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> purchaseOrderService.getById(PO_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() saves PO with DRAFT status and generated number")
    void create_savesAsDraft() {
        Vendor vendor = TestDataBuilder.vendor().id(VENDOR_ID).build();
        CreatePurchaseOrderRequest request = createRequest();

        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID))
                .thenReturn(Optional.of(vendor));

        PurchaseOrder saved = purchaseOrder(POStatus.DRAFT);
        when(poRepository.save(any())).thenReturn(saved);

        PurchaseOrderDto dto = purchaseOrderService.create(request);
        assertThat(dto.getStatus()).isEqualTo(POStatus.DRAFT);
        verify(poRepository).save(any());
    }

    @Test
    @DisplayName("create() throws NOT_FOUND when vendor doesn't exist")
    void create_throwsWhenVendorNotFound() {
        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID))
                .thenReturn(Optional.empty());

        CreatePurchaseOrderRequest request = createRequest();
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> purchaseOrderService.create(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        verify(poRepository, never()).save(any());
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() transitions PO to new status")
    void updateStatus_changesStatus() {
        PurchaseOrder po = purchaseOrder(POStatus.DRAFT);
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.of(po));
        when(poRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderDto dto = purchaseOrderService.updateStatus(PO_ID, POStatus.SENT);
        assertThat(dto.getStatus()).isEqualTo(POStatus.SENT);
    }

    @Test
    @DisplayName("updateStatus() throws BUSINESS_RULE_VIOLATION when cancelling a received PO")
    void updateStatus_throwsOnInvalidTransition() {
        PurchaseOrder po = purchaseOrder(POStatus.RECEIVED);
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.of(po));

        Assertions.assertThrows(AppException.class,
                () -> purchaseOrderService.updateStatus(PO_ID, POStatus.CANCELLED));
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes a DRAFT PO")
    void delete_draftPO() {
        PurchaseOrder po = purchaseOrder(POStatus.DRAFT);
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.of(po));
        when(poRepository.save(any())).thenReturn(po);

        purchaseOrderService.delete(PO_ID);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(poRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws BUSINESS_RULE_VIOLATION for CONFIRMED PO")
    void delete_throwsForConfirmedPO() {
        PurchaseOrder po = purchaseOrder(POStatus.CONFIRMED);
        when(poRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PO_ID))
                .thenReturn(Optional.of(po));

        Assertions.assertThrows(AppException.class, () -> purchaseOrderService.delete(PO_ID));
        verify(poRepository, never()).save(any());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private PurchaseOrder purchaseOrder(POStatus status) {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(PO_ID);
        po.setTenantId(TENANT_ID);
        po.setPoNumber("PO-202605-00001");
        po.setVendorId(VENDOR_ID);
        po.setVendorName("Test Vendor");
        po.setStatus(status);
        po.setOrderDate(LocalDate.now());
        po.setSubtotal(BigDecimal.valueOf(1000));
        po.setTaxAmount(BigDecimal.valueOf(180));
        po.setTotalAmount(BigDecimal.valueOf(1180));
        po.setItems(new ArrayList<>());
        return po;
    }

    private CreatePurchaseOrderRequest createRequest() {
        CreatePurchaseOrderRequest r = new CreatePurchaseOrderRequest();
        r.setVendorId(VENDOR_ID);
        r.setOrderDate(LocalDate.now());
        r.setItems(new ArrayList<>());
        return r;
    }
}
