package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.purchase.dto.CreateGoodsReceiptRequest;
import com.erp.platform.modules.purchase.dto.GoodsReceiptDto;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockService stockService;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<GoodsReceiptDto> list(UUID purchaseOrderId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (purchaseOrderId != null) {
            List<GoodsReceipt> list = goodsReceiptRepository.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(tenantId, purchaseOrderId);
            var page = new org.springframework.data.domain.PageImpl<>(list.stream().map(this::toDto).collect(java.util.stream.Collectors.toList()), pageable, list.size());
            return PageResponse.of(page);
        }
        return PageResponse.of(goodsReceiptRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public GoodsReceiptDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        return toDto(goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Goods receipt not found: " + id)));
    }

    @Transactional
    public GoodsReceiptDto create(CreateGoodsReceiptRequest request) {
        UUID tenantId = tenantContext.current();

        PurchaseOrder po = purchaseOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getPurchaseOrderId())
                .orElseThrow(() -> AppException.notFound("Purchase order not found: " + request.getPurchaseOrderId()));

        GoodsReceipt grn = new GoodsReceipt();
        grn.setTenantId(tenantId);
        grn.setGrnNumber(generateNumber());
        grn.setPurchaseOrderId(po.getId());
        grn.setVendorId(po.getVendorId());
        grn.setVendorName(po.getVendorName());
        grn.setWarehouseId(request.getWarehouseId());
        grn.setReceiptDate(request.getReceiptDate() != null ? request.getReceiptDate() : LocalDate.now());
        grn.setVehicleNumber(request.getVehicleNumber());
        grn.setDriverName(request.getDriverName());
        grn.setLrNumber(request.getLrNumber());
        grn.setDcNumber(request.getDcNumber());
        grn.setDcDate(request.getDcDate());
        grn.setFreightCarrierName(request.getFreightCarrierName());
        grn.setFreightAmount(request.getFreightAmount());
        grn.setFreightAdvancePaid(request.getFreightAdvancePaid());
        grn.setInGatePass(request.getInGatePass());
        grn.setNotes(request.getNotes());
        grn.setStatus("RECEIVED");

        if (request.getItems() != null) {
            List<GoodsReceiptItem> items = new ArrayList<>();
            for (CreateGoodsReceiptRequest.GrnItem r : request.getItems()) {
                // The client may send only the PO line reference, so take the product from the
                // order. A receipt line with no product is invisible to stock, the PO received
                // quantities and the 3-way match, so never store one.
                PurchaseOrderItem poItem = resolvePoItem(po, r);
                GoodsReceiptItem item = new GoodsReceiptItem();
                item.setGoodsReceipt(grn);
                item.setProductId(r.getProductId() != null ? r.getProductId()
                        : poItem != null ? poItem.getProductId() : null);
                if (poItem != null) {
                    item.setProductName(poItem.getProductName());
                    item.setOrderedQty(poItem.getQuantity() != null ? poItem.getQuantity() : BigDecimal.ZERO);
                }
                double received = r.getReceivedQuantity() != null ? r.getReceivedQuantity() : 0;
                double accepted = r.getAcceptedQuantity() != null ? r.getAcceptedQuantity() : received;
                double rejected = received - accepted;
                item.setReceivedQty(BigDecimal.valueOf(received));
                item.setAcceptedQty(BigDecimal.valueOf(accepted));
                item.setRejectedQty(BigDecimal.valueOf(Math.max(0, rejected)));
                items.add(item);
            }
            grn.setItems(items);
        }

        grn = goodsReceiptRepository.save(grn);

        // Auto-update inventory for each accepted item
        final UUID grnId = grn.getId();
        final String grnNumber = grn.getGrnNumber();
        if (grn.getItems() != null) {
            for (GoodsReceiptItem item : grn.getItems()) {
                if (item.getProductId() != null && item.getAcceptedQty() != null
                        && item.getAcceptedQty().compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        stockService.addStock(item.getProductId(), grn.getWarehouseId(), item.getAcceptedQty(),
                                "GRN", grnId, grnNumber, null,
                                "RAW", "GOOD", null, "PURCHASE_RECEIPT");
                    } catch (Exception e) {
                        log.warn("Stock update skipped for product {}: {}", item.getProductId(), e.getMessage());
                    }
                }
            }
        }

        // A PO is only fully RECEIVED once every ordered line has arrived in full. Orders
        // delivered in instalments must stay PARTIALLY_RECEIVED, otherwise they drop out of
        // the "open for receiving" list and the remaining deliveries can never be recorded.
        try {
            boolean complete = allLinesFullyReceived(tenantId, po, grn);
            po.setStatus(complete ? PurchaseOrder.POStatus.RECEIVED : PurchaseOrder.POStatus.PARTIALLY_RECEIVED);
            purchaseOrderRepository.save(po);
            log.info("PO {} updated to {} after GRN {}", po.getPoNumber(), po.getStatus(), grn.getGrnNumber());
        } catch (Exception e) {
            log.warn("PO status update skipped after GRN creation: {}", e.getMessage());
        }

        // Accounting JE: DR Inventory Asset / CR Accounts Payable (DRAFT for accountant review)
        try {
            createGrnAccountingJournalEntry(tenantId, grn, po);
        } catch (Exception e) {
            log.warn("GRN accounting JE skipped for {}: {}", grn.getGrnNumber(), e.getMessage());
        }

        log.info("Goods receipt created: id={}, number={}", grn.getId(), grn.getGrnNumber());
        return toDto(grn);
    }

    /** Matches a requested receipt line back to its purchase order line, by line id then by product. */
    private PurchaseOrderItem resolvePoItem(PurchaseOrder po, CreateGoodsReceiptRequest.GrnItem r) {
        if (po.getItems() == null) return null;
        if (r.getPurchaseOrderItemId() != null) {
            for (PurchaseOrderItem it : po.getItems()) {
                if (r.getPurchaseOrderItemId().equals(it.getId())) return it;
            }
        }
        if (r.getProductId() != null) {
            for (PurchaseOrderItem it : po.getItems()) {
                if (r.getProductId().equals(it.getProductId())) return it;
            }
        }
        return null;
    }

    /** True when every PO line has been received in full across all receipts raised against it. */
    private boolean allLinesFullyReceived(UUID tenantId, PurchaseOrder po, GoodsReceipt justSaved) {
        if (po.getItems() == null || po.getItems().isEmpty()) return true;

        Map<UUID, BigDecimal> receivedByProduct = new HashMap<>();
        List<GoodsReceipt> existing =
                goodsReceiptRepository.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(tenantId, po.getId());
        for (GoodsReceipt g : existing) {
            if (justSaved != null && g.getId() != null && g.getId().equals(justSaved.getId())) continue;
            tallyReceived(g, receivedByProduct);
        }
        tallyReceived(justSaved, receivedByProduct);

        for (PurchaseOrderItem item : po.getItems()) {
            BigDecimal ordered = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal received = receivedByProduct.getOrDefault(item.getProductId(), BigDecimal.ZERO);
            if (received.compareTo(ordered) < 0) return false;
        }
        return true;
    }

    private void tallyReceived(GoodsReceipt grn, Map<UUID, BigDecimal> receivedByProduct) {
        if (grn == null || grn.getItems() == null) return;
        for (GoodsReceiptItem it : grn.getItems()) {
            if (it.getProductId() == null) continue;
            BigDecimal qty = it.getAcceptedQty() != null ? it.getAcceptedQty()
                    : it.getReceivedQty() != null ? it.getReceivedQty() : BigDecimal.ZERO;
            receivedByProduct.merge(it.getProductId(), qty, BigDecimal::add);
        }
    }

    private void createGrnAccountingJournalEntry(UUID tenantId, GoodsReceipt grn, PurchaseOrder po) {
        List<Account> invAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "INVENTORY_ASSET");
        List<Account> apAccounts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_PAYABLE");
        if (invAccounts.isEmpty() || apAccounts.isEmpty()) {
            log.debug("GRN accounting JE skipped for {}: accounts not configured", grn.getGrnNumber());
            return;
        }
        BigDecimal amount = po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) == 0) return;
        Account invAccount = invAccounts.get(0);
        Account apAccount  = apAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("GRN");
        je.setReferenceId(grn.getId());
        je.setReferenceNumber(grn.getGrnNumber());
        je.setDescription("Goods Receipt: " + grn.getGrnNumber() + " — " + grn.getVendorName());
        je.setEntryDate(grn.getReceiptDate());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(invAccount.getId());
        drLine.setAccountCode(invAccount.getCode());
        drLine.setAccountName(invAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("Inventory receipt — " + grn.getGrnNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(apAccount.getId());
        crLine.setAccountCode(apAccount.getCode());
        crLine.setAccountName(apAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("Accounts payable — " + grn.getVendorName());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("GRN accounting JE (DRAFT) created for {}", grn.getGrnNumber());
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        GoodsReceipt grn = goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Goods receipt not found: " + id));
        grn.setDeletedAt(LocalDateTime.now());
        goodsReceiptRepository.save(grn);
    }

    private GoodsReceiptDto toDto(GoodsReceipt grn) {
        GoodsReceiptDto dto = new GoodsReceiptDto();
        dto.setId(grn.getId());
        dto.setTenantId(grn.getTenantId());
        dto.setGrnNumber(grn.getGrnNumber());
        dto.setPurchaseOrderId(grn.getPurchaseOrderId());
        dto.setSourceIntakeSlipId(grn.getSourceIntakeSlipId());
        dto.setVendorId(grn.getVendorId());
        dto.setVendorName(grn.getVendorName());
        dto.setReceiptDate(grn.getReceiptDate());
        dto.setStatus(grn.getStatus());
        dto.setVehicleNumber(grn.getVehicleNumber());
        dto.setDriverName(grn.getDriverName());
        dto.setLrNumber(grn.getLrNumber());
        dto.setDcNumber(grn.getDcNumber());
        dto.setDcDate(grn.getDcDate());
        dto.setFreightCarrierName(grn.getFreightCarrierName());
        dto.setFreightAmount(grn.getFreightAmount());
        dto.setFreightAdvancePaid(grn.getFreightAdvancePaid());
        dto.setInGatePass(grn.getInGatePass());
        dto.setNotes(grn.getNotes());
        dto.setCreatedAt(grn.getCreatedAt());

        if (grn.getItems() != null && !grn.getItems().isEmpty()) {
            PurchaseOrder po = null;
            if (grn.getPurchaseOrderId() != null) {
                po = purchaseOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(grn.getTenantId(), grn.getPurchaseOrderId()).orElse(null);
            }
            final PurchaseOrder finalPo = po;
            List<GoodsReceiptDto.GrnItemDto> itemDtos = new ArrayList<>();
            for (GoodsReceiptItem item : grn.getItems()) {
                GoodsReceiptDto.GrnItemDto itemDto = new GoodsReceiptDto.GrnItemDto();
                itemDto.setId(item.getId());
                itemDto.setProductId(item.getProductId());

                String pName = item.getProductName();
                BigDecimal ordQty = item.getOrderedQty();
                if (finalPo != null && finalPo.getItems() != null) {
                    for (var poItem : finalPo.getItems()) {
                        if (poItem.getProductId() != null && poItem.getProductId().equals(item.getProductId())) {
                            if (pName == null || pName.isBlank()) pName = poItem.getProductName();
                            if (ordQty == null || ordQty.compareTo(BigDecimal.ZERO) == 0) ordQty = poItem.getQuantity();
                            break;
                        }
                    }
                }
                itemDto.setProductName(pName != null && !pName.isBlank() ? pName : "Item");
                itemDto.setOrderedQty(ordQty != null ? ordQty : BigDecimal.ZERO);
                itemDto.setReceivedQty(item.getReceivedQty() != null ? item.getReceivedQty() : BigDecimal.ZERO);
                itemDto.setAcceptedQty(item.getAcceptedQty() != null ? item.getAcceptedQty() : itemDto.getReceivedQty());
                itemDto.setRejectedQty(item.getRejectedQty() != null ? item.getRejectedQty() : BigDecimal.ZERO);
                itemDtos.add(itemDto);
            }
            dto.setItems(itemDtos);
        }
        return dto;
    }

    private String generateNumber() {
        return "GRN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
