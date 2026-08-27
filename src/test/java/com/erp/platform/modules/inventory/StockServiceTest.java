package com.erp.platform.modules.inventory;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.StockItemDto;
import com.erp.platform.modules.inventory.entity.StockItem;
import com.erp.platform.modules.inventory.entity.StockMovement;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.inventory.repository.StockTransactionRepository;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
import com.erp.platform.modules.inventory.service.StockService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService unit tests")
class StockServiceTest {

    @Mock private StockItemRepository stockItemRepository;
    @Mock private StockTransactionRepository movementRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private StockService stockService;

    private static final UUID TENANT_ID    = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID   = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    // ─── createWarehouse ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createWarehouse() saves warehouse with correct tenantId")
    void createWarehouse_setsTenantId() {
        var request = new com.erp.platform.modules.inventory.dto.CreateWarehouseRequest();
        request.setName("Main Warehouse");
        request.setCode("WH-001");

        Warehouse saved = new Warehouse();
        saved.setId(WAREHOUSE_ID);
        saved.setTenantId(TENANT_ID);
        saved.setName("Main Warehouse");

        when(warehouseRepository.save(any())).thenReturn(saved);

        stockService.createWarehouse(request);

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    // ─── addStock ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addStock() increases quantityOnHand")
    void addStock_increasesQuantity() {
        Warehouse wh = warehouse();
        StockItem existing = stockItem(BigDecimal.valueOf(10));

        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.of(existing));
        when(stockItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        stockService.addStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.valueOf(5), "PURCHASE", UUID.randomUUID(), "PO-001");

        ArgumentCaptor<StockItem> captor = ArgumentCaptor.forClass(StockItem.class);
        verify(stockItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    @DisplayName("addStock() creates StockItem when it doesn't exist")
    void addStock_createsNewStockItem() {
        Warehouse wh = warehouse();

        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.empty());
        when(stockItemRepository.findFirstByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(stockItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        stockService.addStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.valueOf(20), "PURCHASE", null, null);

        verify(stockItemRepository).save(argThat(s -> s.getQuantityOnHand().compareTo(BigDecimal.valueOf(20)) == 0));
    }

    @Test
    @DisplayName("addStock() records a RECEIPT movement")
    void addStock_recordsReceiptMovement() {
        Warehouse wh = warehouse();
        StockItem existing = stockItem(BigDecimal.ZERO);

        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.of(existing));
        when(stockItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        stockService.addStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.valueOf(10), "PURCHASE", null, null);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(StockMovement.MovementType.RECEIPT);
    }

    // ─── deductStock ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deductStock() decreases quantityOnHand")
    void deductStock_decreasesQuantity() {
        Warehouse wh = warehouse();
        StockItem existing = stockItem(BigDecimal.valueOf(50));

        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.of(existing));
        when(stockItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        stockService.deductStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.valueOf(20), "SALES", null, null);

        ArgumentCaptor<StockItem> captor = ArgumentCaptor.forClass(StockItem.class);
        verify(stockItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    @DisplayName("deductStock() throws INSUFFICIENT_STOCK when not enough stock")
    void deductStock_throwsWhenInsufficient() {
        Warehouse wh = warehouse();
        StockItem existing = stockItem(BigDecimal.valueOf(5));

        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.of(existing));

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> stockService.deductStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.valueOf(10), "SALES", null, null));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        verify(stockItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("deductStock() throws NOT_FOUND when no stock record exists")
    void deductStock_throwsWhenNoStockRecord() {
        Warehouse wh = warehouse();
        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID)).thenReturn(Optional.of(wh));
        when(stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(eq(WAREHOUSE_ID), eq(PRODUCT_ID), anyString(), anyString())).thenReturn(Optional.empty());
        when(stockItemRepository.findFirstByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(AppException.class,
                () -> stockService.deductStock(PRODUCT_ID, WAREHOUSE_ID, BigDecimal.TEN, "SALES", null, null));
    }

    // ─── adjust ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("adjust() sets exact new quantity and records ADJUSTMENT movement")
    void adjust_setsExactQuantity() {
        when(warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(warehouse()));
        when(stockItemRepository.findFirstByWarehouseIdAndProductId(WAREHOUSE_ID, PRODUCT_ID))
                .thenReturn(Optional.of(stockItem(BigDecimal.valueOf(30))));
        when(stockItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        stockService.adjust(WAREHOUSE_ID, PRODUCT_ID, BigDecimal.valueOf(45), "Physical count correction");

        ArgumentCaptor<StockItem> captor = ArgumentCaptor.forClass(StockItem.class);
        verify(stockItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityOnHand()).isEqualByComparingTo(BigDecimal.valueOf(45));

        ArgumentCaptor<StockMovement> movCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(movCaptor.capture());
        assertThat(movCaptor.getValue().getType()).isEqualTo(StockMovement.MovementType.ADJUSTMENT);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Warehouse warehouse() {
        Warehouse w = new Warehouse();
        w.setId(WAREHOUSE_ID);
        w.setTenantId(TENANT_ID);
        w.setName("Main Warehouse");
        w.setActive(true);
        return w;
    }

    private StockItem stockItem(BigDecimal qty) {
        StockItem s = new StockItem();
        s.setId(UUID.randomUUID());
        s.setTenantId(TENANT_ID);
        s.setWarehouseId(WAREHOUSE_ID);
        s.setProductId(PRODUCT_ID);
        s.setQuantityOnHand(qty);
        s.setQuantityReserved(BigDecimal.ZERO);
        s.setAverageCost(BigDecimal.valueOf(50));
        return s;
    }
}
