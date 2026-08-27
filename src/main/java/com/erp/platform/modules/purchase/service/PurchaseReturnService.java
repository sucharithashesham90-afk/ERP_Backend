package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.purchase.dto.CreatePurchaseReturnRequest;
import com.erp.platform.modules.purchase.dto.PurchaseReturnDto;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.purchase.entity.PurchaseReturn;
import com.erp.platform.modules.purchase.entity.PurchaseReturnItem;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.repository.PurchaseInvoiceRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import com.erp.platform.modules.purchase.repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final VendorRepository vendorRepository;
    private final StockService stockService;
    /** A seed return names the lot it is going back from, and that lot's balance has to fall. */
    private final com.erp.platform.modules.inventory.repository.StockLotRepository stockLotRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;
    /** Purchase invoices keep their lines as JSON, so pricing a return has to read them back. */
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    public PageResponse<PurchaseReturnDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(purchaseReturnRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    @Transactional
    public PurchaseReturnDto create(CreatePurchaseReturnRequest request) {
        UUID tenantId = tenantContext.current();

        final PurchaseReturn ret = new PurchaseReturn();
        ret.setTenantId(tenantId);
        ret.setReturnNumber(generateNumber());
        ret.setReturnDate(LocalDate.now());
        ret.setReason(request.getReason());
        ret.setNotes(request.getNotes());

        if (request.getGoodsReceiptId() != null) {
            goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getGoodsReceiptId())
                    .ifPresent(grn -> {
                        ret.setGoodsReceiptId(grn.getId());
                        ret.setVendorId(grn.getVendorId());
                        ret.setVendorName(grn.getVendorName());
                    });
        }
        if (request.getInvoiceId() != null) {
            purchaseInvoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getInvoiceId())
                    .ifPresent(inv -> {
                        ret.setInvoiceId(inv.getId());
                        if (inv.getVendorId() != null) ret.setVendorId(inv.getVendorId());
                        if (inv.getVendorName() != null) ret.setVendorName(inv.getVendorName());
                    });
        }
        if (ret.getVendorId() == null && request.getVendorId() != null) {
            ret.setVendorId(request.getVendorId());
            ret.setVendorName(request.getVendorName());
        }

        applyReturnHeader(ret, request);
        ret.setStatus("DRAFT");
        ret.setDebitNoteStatus("NOT_ISSUED");

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Goods receipts carry no price, so a return raised from a GRN arrives with a
            // zero unit price. Fall back to the originating purchase order line, otherwise
            // every line totals zero and the debit note is skipped as a nil-value document.
            Map<UUID, PurchaseOrderItem> poItemsByProduct = loadPurchaseOrderItems(tenantId, ret.getGoodsReceiptId());
            PriceBook priceBook = buildPriceBook(tenantId, ret);

            List<PurchaseReturnItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (CreatePurchaseReturnRequest.ReturnItem r : request.getItems()) {
                PurchaseOrderItem poItem = r.getProductId() != null ? poItemsByProduct.get(r.getProductId()) : null;

                PurchaseReturnItem item = new PurchaseReturnItem();
                item.setPurchaseReturn(ret);
                item.setGoodsReceiptItemId(r.getGoodsReceiptItemId());
                item.setProductId(r.getProductId());
                item.setProductName(r.getProductName());
                item.setCropGroupId(r.getCropGroupId());
                item.setCropGroupName(r.getCropGroupName());
                item.setCropId(r.getCropId());
                item.setCropName(firstNonBlank(r.getCropName(), poItem != null ? poItem.getCropGroupName() : null));
                item.setVarietyId(r.getVarietyId());
                item.setVarietyName(firstNonBlank(r.getVarietyName(), poItem != null ? poItem.getVarietyName() : null));
                item.setLotNumber(r.getLotNumber());
                item.setWarehouseId(r.getWarehouseId());
                item.setWarehouseName(r.getWarehouseName());
                item.setUnit(firstNonBlank(r.getUnit(), poItem != null ? poItem.getUnit() : null));

                BigDecimal qty = r.getReturnQty() != null ? r.getReturnQty()
                        : (r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO);
                // Price the line at what it was bought for. Only fall back to what the client sent
                // when nothing upstream knows the item — a return typed from scratch against no
                // document has no purchase to inherit from.
                BigDecimal price = priceBook.find(r.getProductId(),
                        r.getProductName(), r.getVarietyName(), r.getCropName());
                if (price == null && poItem != null) price = poItem.getUnitPrice();
                if (price == null || price.signum() <= 0) price = r.getUnitPrice();
                if (price == null) price = BigDecimal.ZERO;
                if (price.signum() > 0 && (r.getUnitPrice() == null || r.getUnitPrice().signum() <= 0)) {
                    log.debug("Return line {} priced at {} from the originating purchase",
                            firstNonBlank(r.getProductName(), r.getVarietyName()), price);
                }

                BigDecimal lineAmt = qty.multiply(price);
                item.setReturnQty(qty);
                item.setUnitPrice(price);
                item.setAmount(lineAmt);
                item.setReason(firstNonBlank(r.getReason(), r.getRemarks()));
                items.add(item);
                total = total.add(lineAmt);
            }
            ret.setItems(items);
            ret.setTotalAmount(total);
        }

        // A debit note is a demand on the supplier for money. Raising one for zero — which is what
        // happened whenever the lines carried no price — puts a meaningless document in front of
        // the vendor and in the ledger. The return is still saved; the note waits for a price.
        boolean hasValue = ret.getTotalAmount() != null && ret.getTotalAmount().signum() > 0;
        if (ret.getDebitNoteNumber() == null && hasValue) {
            ret.setDebitNoteNumber("DN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            ret.setDebitNoteDate(LocalDate.now());
            ret.setDebitNoteStatus("ISSUED");
        } else if (!hasValue) {
            ret.setDebitNoteStatus("PENDING_VALUE");
            log.warn("Purchase return {} has no value — debit note withheld until the lines are priced",
                    ret.getReturnNumber());
        }

        PurchaseReturn saved = purchaseReturnRepository.save(ret);
        log.info("Purchase return created: id={}, number={}, total={}",
                saved.getId(), saved.getReturnNumber(), saved.getTotalAmount());
        if (hasValue) {
            try {
                createDebitNoteJournalEntry(tenantId, saved);
            } catch (Exception e) {
                log.warn("Debit note generation on creation skipped for {}: {}", saved.getReturnNumber(), e.getMessage());
            }
        }
        return toDto(saved);
    }

    @Transactional
    public PurchaseReturnDto approve(UUID id) {
        UUID tenantId = tenantContext.current();
        PurchaseReturn ret = purchaseReturnRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Purchase return not found: " + id));
        if (!"DRAFT".equals(ret.getStatus())) {
            throw AppException.badRequest("Only DRAFT purchase returns can be approved");
        }
        final UUID retId     = ret.getId();
        final String retNum  = ret.getReturnNumber();

        // Send back exactly what the return says, from the lots it names.
        //
        // This used to deduct every accepted line of the original goods receipt instead, which is a
        // different quantity entirely: returning two bags of a fifty-bag delivery removed all fifty.
        // It also ignored the lot, so on a seed return the stock left the wrong lot — and lot
        // identity is the whole point of seed inventory.
        boolean deductedFromReturn = false;
        for (PurchaseReturnItem item : ret.getItems() == null ? List.<PurchaseReturnItem>of() : ret.getItems()) {
            if (item.getReturnQty() == null || item.getReturnQty().signum() <= 0) continue;
            deductedFromReturn = true;

            // A named lot is deducted from that lot, so the balance that falls is the one the
            // inspector can see on the shelf.
            if (item.getLotNumber() != null && !item.getLotNumber().isBlank()) {
                try {
                    stockLotRepository.findByTenantIdAndLotNoAndDeletedAtIsNull(tenantId, item.getLotNumber())
                            .stream().findFirst().ifPresent(lot -> {
                                BigDecimal have = lot.getQuantity() == null ? BigDecimal.ZERO : lot.getQuantity();
                                lot.setQuantity(have.subtract(item.getReturnQty()).max(BigDecimal.ZERO));
                                stockLotRepository.save(lot);
                            });
                } catch (Exception e) {
                    log.warn("Lot deduction skipped for lot {} on PR {}: {}",
                            item.getLotNumber(), retNum, e.getMessage());
                }
            }

            if (item.getProductId() == null) continue;
            try {
                stockService.deductStock(item.getProductId(), item.getWarehouseId(), item.getReturnQty(),
                        "PURCHASE_RETURN", retId, retNum,
                        "RAW", "GOOD", item.getLotNumber(), "PURCHASE_RETURN");
            } catch (Exception e) {
                log.warn("Stock deduction skipped for product {} on PR {}: {}",
                        item.getProductId(), retNum, e.getMessage());
            }
        }

        // Only fall back to the receipt when the return carries no lines of its own — an older
        // record created before returns had items.
        if (!deductedFromReturn && ret.getGoodsReceiptId() != null) {
            goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ret.getGoodsReceiptId())
                    .ifPresent(grn -> {
                        for (GoodsReceiptItem item : grn.getItems()) {
                            if (item.getProductId() == null || item.getAcceptedQty() == null
                                    || item.getAcceptedQty().signum() <= 0) continue;
                            try {
                                stockService.deductStock(item.getProductId(), null, item.getAcceptedQty(),
                                        "PURCHASE_RETURN", retId, retNum,
                                        "RAW", "GOOD", null, "PURCHASE_RETURN");
                            } catch (Exception e) {
                                log.warn("Stock deduction skipped for product {} on PR {}: {}",
                                        item.getProductId(), retNum, e.getMessage());
                            }
                        }
                    });
        }
        ret.setStatus("APPROVED");
        ret.setDebitNoteNumber("DN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        ret.setDebitNoteDate(LocalDate.now());
        ret.setDebitNoteStatus("ISSUED");
        final PurchaseReturn savedRet = purchaseReturnRepository.save(ret);
        log.info("Purchase return approved: id={}, number={}", savedRet.getId(), savedRet.getReturnNumber());
        try {
            createDebitNoteJournalEntry(tenantId, savedRet);
        } catch (Exception e) {
            log.warn("Debit note JE skipped for {}: {}", savedRet.getReturnNumber(), e.getMessage());
        }
        // Reduce vendor outstanding balance by the return amount
        goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, savedRet.getGoodsReceiptId())
                .ifPresent(grn -> {
                    if (grn.getVendorId() == null) return;
                    BigDecimal returnAmount = savedRet.getTotalAmount() != null ? savedRet.getTotalAmount() : BigDecimal.ZERO;
                    if (returnAmount.compareTo(BigDecimal.ZERO) <= 0) return;
                    vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, grn.getVendorId()).ifPresent(vendor -> {
                        BigDecimal current = vendor.getOutstandingBalance() != null ? vendor.getOutstandingBalance() : BigDecimal.ZERO;
                        vendor.setOutstandingBalance(current.subtract(returnAmount).max(BigDecimal.ZERO));
                        vendorRepository.save(vendor);
                        log.info("Vendor {} outstanding reduced by {} after purchase return {}", vendor.getName(), returnAmount, savedRet.getReturnNumber());
                    });
                });
        return toDto(savedRet);
    }

    /** Purchase order lines for the receipt being returned, keyed by product. */
    /**
     * What each item was bought at, gathered from whatever the return was raised against.
     *
     * <p>The price of a return is not a new decision — it is the price the goods were purchased at,
     * already agreed on the order and already posted to the ledger by the invoice. Asking the user
     * to retype it invites a different number and a debit note that does not reconcile.
     *
     * <p>Sources are consulted in order of authority: the purchase invoice (what was actually
     * billed), then the purchase order (what was agreed). Invoice lines carry no product id, so
     * names are indexed too — that is all a goods receipt gives us to match on.
     */
    private static final class PriceBook {
        private final Map<UUID, BigDecimal> byProductId = new HashMap<>();
        private final Map<String, BigDecimal> byName = new HashMap<>();

        void put(UUID productId, String name, BigDecimal price) {
            if (price == null || price.signum() <= 0) return;
            if (productId != null) byProductId.putIfAbsent(productId, price);
            if (name != null && !name.isBlank()) byName.putIfAbsent(name.trim().toLowerCase(), price);
        }

        BigDecimal find(UUID productId, String... names) {
            if (productId != null) {
                BigDecimal p = byProductId.get(productId);
                if (p != null) return p;
            }
            for (String n : names) {
                if (n == null || n.isBlank()) continue;
                BigDecimal p = byName.get(n.trim().toLowerCase());
                if (p != null) return p;
            }
            return null;
        }
    }

    private PriceBook buildPriceBook(UUID tenantId, PurchaseReturn ret) {
        PriceBook book = new PriceBook();

        // The invoice is what the vendor actually billed, so it wins.
        if (ret.getInvoiceId() != null) {
            purchaseInvoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ret.getInvoiceId())
                    .ifPresent(inv -> {
                        for (Map<String, Object> line : parseInvoiceItems(inv.getItemsJson())) {
                            book.put(null,
                                    str(line.get("productName")),
                                    toDecimal(line.get("unitPrice")));
                            book.put(null,
                                    str(line.get("varietyName")),
                                    toDecimal(line.get("unitPrice")));
                        }
                    });
        }

        // Then the order — reached directly, or through the receipt the goods came in on.
        UUID poId = null;
        if (ret.getGoodsReceiptId() != null) {
            poId = goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ret.getGoodsReceiptId())
                    .map(GoodsReceipt::getPurchaseOrderId).orElse(null);
        }
        if (poId == null && ret.getInvoiceId() != null) {
            poId = purchaseInvoiceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ret.getInvoiceId())
                    .map(PurchaseInvoice::getPurchaseOrderId).orElse(null);
        }
        if (poId != null) {
            purchaseOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, poId).ifPresent(po -> {
                for (PurchaseOrderItem poItem : po.getItems()) {
                    book.put(poItem.getProductId(), poItem.getProductName(), poItem.getUnitPrice());
                    book.put(null, poItem.getVarietyName(), poItem.getUnitPrice());
                }
            });
        }

        // A return raised against the vendor alone — no receipt, no invoice — still concerns goods
        // that were bought from them at some price. Fall back to their recent orders, newest first,
        // so the most recently agreed price wins. Without this such a return can only ever be nil.
        if (ret.getVendorId() != null) {
            purchaseOrderRepository
                    .findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, ret.getVendorId(),
                            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "orderDate")))
                    .forEach(po -> {
                        for (PurchaseOrderItem poItem : po.getItems()) {
                            book.put(poItem.getProductId(), poItem.getProductName(), poItem.getUnitPrice());
                            book.put(null, poItem.getVarietyName(), poItem.getUnitPrice());
                        }
                    });
        }
        return book;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseInvoiceItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(itemsJson, List.class);
        } catch (Exception e) {
            log.warn("Could not read invoice items for pricing a return: {}", e.getMessage());
            return List.of();
        }
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }

    private static BigDecimal toDecimal(Object o) {
        if (o == null) return null;
        try { return new BigDecimal(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Map<UUID, PurchaseOrderItem> loadPurchaseOrderItems(UUID tenantId, UUID goodsReceiptId) {
        if (goodsReceiptId == null) return Map.of();
        return goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, goodsReceiptId)
                .map(GoodsReceipt::getPurchaseOrderId)
                .flatMap(poId -> poId == null ? Optional.empty()
                        : purchaseOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, poId))
                .map(po -> {
                    Map<UUID, PurchaseOrderItem> byProduct = new HashMap<>();
                    for (PurchaseOrderItem poItem : po.getItems()) {
                        if (poItem.getProductId() != null) byProduct.putIfAbsent(poItem.getProductId(), poItem);
                    }
                    return byProduct;
                })
                .orElseGet(Map::of);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private void createDebitNoteJournalEntry(UUID tenantId, PurchaseReturn ret) {
        List<Account> apAccounts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "ACCOUNTS_PAYABLE");
        List<Account> expAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "PURCHASE_EXPENSE");
        if (apAccounts.isEmpty() || expAccounts.isEmpty()) {
            log.warn("Debit note JE skipped for {}: no ACCOUNTS_PAYABLE and/or PURCHASE_EXPENSE account "
                    + "is configured for this tenant — configure them in Chart of Accounts", ret.getReturnNumber());
            return;
        }
        BigDecimal amount = ret.getTotalAmount() != null ? ret.getTotalAmount() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Debit note JE skipped for {}: return total is zero — check that the returned lines "
                    + "carry a unit price", ret.getReturnNumber());
            return;
        }
        Account apAccount  = apAccounts.get(0);
        Account expAccount = expAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        // What this posting *is* is a debit note, so it belongs in the Debit Note book — booking it
        // under PURCHASE_RETURN filed the note in the returns book and left the debit note register
        // empty.
        je.setReferenceType("DEBIT_NOTE");
        je.setReferenceId(ret.getId());
        je.setReferenceNumber(ret.getDebitNoteNumber() != null ? ret.getDebitNoteNumber() : ret.getReturnNumber());
        je.setDescription("Debit Note " + (ret.getDebitNoteNumber() != null ? ret.getDebitNoteNumber() : "")
                + " for purchase return " + ret.getReturnNumber() + " — " + ret.getVendorName());
        je.setEntryDate(ret.getReturnDate());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(apAccount.getId());
        drLine.setAccountCode(apAccount.getCode());
        drLine.setAccountName(apAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("AP reduction — " + ret.getReturnNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(expAccount.getId());
        crLine.setAccountCode(expAccount.getCode());
        crLine.setAccountName(expAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("Purchase expense reversal — " + ret.getReturnNumber());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        // Posted, not left in DRAFT: a debit note that never reaches the ledger reduces nothing the
        // vendor owes, so the payable stayed overstated after every return.
        JournalEntry created = journalEntryService.create(je);
        journalEntryService.post(created.getId());
        ret.setDebitNoteJournalEntryId(created.getId());
        ret.setDebitNoteJournalEntryNumber(created.getEntryNumber());
        log.info("Debit note {} posted for purchase return {} (voucher {})",
                ret.getDebitNoteNumber(), ret.getReturnNumber(), created.getEntryNumber());
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        PurchaseReturn ret = purchaseReturnRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Purchase return not found: " + id));
        ret.setDeletedAt(LocalDateTime.now());
        purchaseReturnRepository.save(ret);
    }

    /** Copy the return logistics header fields from request onto the entity. */
    private void applyReturnHeader(PurchaseReturn ret, CreatePurchaseReturnRequest r) {
        ret.setReturnType(r.getReturnType());
        ret.setPrLocation(r.getPrLocation());
        ret.setWayBillNumber(r.getWayBillNumber());
        ret.setReturnValue(r.getReturnValue());
        ret.setRrRlNumber(r.getRrRlNumber());
        ret.setCarrier(r.getCarrier());
        ret.setLorryNumber(r.getLorryNumber());
        ret.setFreightTotal(r.getFreightTotal());
        ret.setFreightPaidAdvance(r.getFreightPaidAdvance());
        ret.setFreightToPay(r.getFreightToPay());
        ret.setBillingAddress(r.getBillingAddress());
        ret.setBillingState(r.getBillingState());
        ret.setBillingDistrict(r.getBillingDistrict());
        ret.setBillingCity(r.getBillingCity());
        ret.setBillingZip(r.getBillingZip());
        ret.setBillingPhone(r.getBillingPhone());
        ret.setDeliveryAddress(r.getDeliveryAddress());
        ret.setDeliveryState(r.getDeliveryState());
        ret.setDeliveryDistrict(r.getDeliveryDistrict());
        ret.setDeliveryCity(r.getDeliveryCity());
        ret.setDeliveryZip(r.getDeliveryZip());
        ret.setDeliveryPhone(r.getDeliveryPhone());
    }

    private PurchaseReturnDto toDto(PurchaseReturn ret) {
        PurchaseReturnDto dto = new PurchaseReturnDto();
        dto.setId(ret.getId());
        dto.setTenantId(ret.getTenantId());
        dto.setReturnNumber(ret.getReturnNumber());
        dto.setGoodsReceiptId(ret.getGoodsReceiptId());
        dto.setVendorId(ret.getVendorId());
        dto.setVendorName(ret.getVendorName());
        dto.setReturnDate(ret.getReturnDate());
        dto.setReason(ret.getReason());
        dto.setNotes(ret.getNotes());
        dto.setTotalAmount(ret.getTotalAmount());
        dto.setStatus(ret.getStatus());
        dto.setDebitNoteNumber(ret.getDebitNoteNumber());
        dto.setDebitNoteDate(ret.getDebitNoteDate());
        dto.setDebitNoteStatus(ret.getDebitNoteStatus());
        dto.setReturnType(ret.getReturnType());
        dto.setPrLocation(ret.getPrLocation());
        dto.setWayBillNumber(ret.getWayBillNumber());
        dto.setReturnValue(ret.getReturnValue());
        dto.setRrRlNumber(ret.getRrRlNumber());
        dto.setCarrier(ret.getCarrier());
        dto.setLorryNumber(ret.getLorryNumber());
        dto.setFreightTotal(ret.getFreightTotal());
        dto.setFreightPaidAdvance(ret.getFreightPaidAdvance());
        dto.setFreightToPay(ret.getFreightToPay());
        dto.setBillingAddress(ret.getBillingAddress());
        dto.setBillingState(ret.getBillingState());
        dto.setBillingDistrict(ret.getBillingDistrict());
        dto.setBillingCity(ret.getBillingCity());
        dto.setBillingZip(ret.getBillingZip());
        dto.setBillingPhone(ret.getBillingPhone());
        dto.setDeliveryAddress(ret.getDeliveryAddress());
        dto.setDeliveryState(ret.getDeliveryState());
        dto.setDeliveryDistrict(ret.getDeliveryDistrict());
        dto.setDeliveryCity(ret.getDeliveryCity());
        dto.setDeliveryZip(ret.getDeliveryZip());
        dto.setDeliveryPhone(ret.getDeliveryPhone());
        if (ret.getItems() != null) {
            dto.setItems(ret.getItems().stream().map(i -> {
                PurchaseReturnDto.ItemDto d = new PurchaseReturnDto.ItemDto();
                d.setId(i.getId());
                d.setGoodsReceiptItemId(i.getGoodsReceiptItemId());
                d.setProductId(i.getProductId());
                d.setProductName(i.getProductName());
                d.setCropId(i.getCropId());
                d.setCropGroupName(i.getCropGroupName());
            d.setCropName(i.getCropName());
                d.setVarietyId(i.getVarietyId());
                d.setVarietyName(i.getVarietyName());
                d.setLotNumber(i.getLotNumber());
                d.setWarehouseId(i.getWarehouseId());
                d.setWarehouseName(i.getWarehouseName());
                d.setUnit(i.getUnit());
                d.setReturnQty(i.getReturnQty());
                d.setQuantity(i.getReturnQty());
                d.setUnitPrice(i.getUnitPrice());
                d.setAmount(i.getAmount());
                d.setReason(i.getReason());
                return d;
            }).collect(Collectors.toList()));
        }
        dto.setCreatedAt(ret.getCreatedAt());
        return dto;
    }

    private String generateNumber() {
        return "PR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
