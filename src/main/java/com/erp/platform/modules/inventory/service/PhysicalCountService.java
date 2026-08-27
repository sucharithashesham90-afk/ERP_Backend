package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.inventory.dto.CreatePhysicalCountRequest;
import com.erp.platform.modules.inventory.dto.PhysicalCountDto;
import com.erp.platform.modules.inventory.entity.PhysicalCount;
import com.erp.platform.modules.inventory.entity.PhysicalCountItem;
import com.erp.platform.modules.inventory.entity.StockItem;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.PhysicalCountRepository;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PhysicalCountService {

    private final PhysicalCountRepository physicalCountRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockItemRepository stockItemRepository;
    private final StockService stockService;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<PhysicalCountDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(physicalCountRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PhysicalCountDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        PhysicalCount count = physicalCountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Physical count not found: " + id));
        return toDto(count);
    }

    @Transactional
    public PhysicalCountDto create(CreatePhysicalCountRequest request) {
        UUID tenantId = tenantContext.current();

        Warehouse warehouse = warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getWarehouseId())
                .orElseThrow(() -> AppException.notFound("Warehouse not found: " + request.getWarehouseId()));

        PhysicalCount count = new PhysicalCount();
        count.setTenantId(tenantId);
        count.setCountNumber(generateNumber());
        count.setWarehouseId(warehouse.getId());
        count.setWarehouseName(warehouse.getName());
        count.setCountDate(request.getCountDate() != null ? request.getCountDate() : LocalDate.now());
        count.setNotes(request.getNotes());
        count.setStatus("DRAFT");

        if (request.getItems() != null) {
            List<PhysicalCountItem> items = new ArrayList<>();
            for (CreatePhysicalCountRequest.ItemRequest r : request.getItems()) {
                if (r.getProductId() == null) continue;
                PhysicalCountItem item = new PhysicalCountItem();
                item.setTenantId(tenantId);
                item.setPhysicalCount(count);
                item.setProductId(r.getProductId());
                item.setProductName(r.getProductName());
                item.setCountedQuantity(r.getCountedQuantity() != null ? r.getCountedQuantity() : BigDecimal.ZERO);
                item.setUnit(r.getUnit());
                item.setRemarks(r.getRemarks());

                // Populate system quantity from current stock
                BigDecimal systemQty = stockItemRepository
                        .findFirstByWarehouseIdAndProductId(warehouse.getId(), r.getProductId())
                        .map(StockItem::getQuantityOnHand)
                        .orElse(BigDecimal.ZERO);
                item.setSystemQuantity(systemQty);
                item.setDifferenceQuantity(item.getCountedQuantity().subtract(systemQty));
                items.add(item);
            }
            count.setItems(items);
        }

        count = physicalCountRepository.save(count);
        log.info("Physical count created: id={}, number={}", count.getId(), count.getCountNumber());
        return toDto(count);
    }

    @Transactional
    public PhysicalCountDto submit(UUID id) {
        UUID tenantId = tenantContext.current();
        PhysicalCount count = physicalCountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Physical count not found: " + id));

        if (!"DRAFT".equals(count.getStatus())) {
            throw AppException.badRequest("Only DRAFT counts can be submitted");
        }

        List<PhysicalCountItem> items = count.getItems();
        if (items == null || items.isEmpty()) {
            throw AppException.badRequest("Physical count has no items");
        }

        for (PhysicalCountItem item : items) {
            if (item.getProductId() == null) continue;
            if (item.getCountedQuantity().compareTo(BigDecimal.ZERO) < 0) continue;
            try {
                stockService.adjust(count.getWarehouseId(), item.getProductId(),
                        item.getCountedQuantity(),
                        "Physical count " + count.getCountNumber());
                log.info("Stock adjusted for physical count {}: product={}, newQty={}",
                        count.getCountNumber(), item.getProductId(), item.getCountedQuantity());
            } catch (Exception e) {
                log.warn("Stock adjustment skipped for product {}: {}", item.getProductId(), e.getMessage());
            }
        }

        // Post variance journal entries for stock gain/loss
        try {
            createVarianceJournalEntries(tenantId, count, items);
        } catch (Exception e) {
            log.warn("Variance JE skipped for count {}: {}", count.getCountNumber(), e.getMessage());
        }

        count.setStatus("COMPLETED");
        count = physicalCountRepository.save(count);
        log.info("Physical count submitted: id={}, number={}", count.getId(), count.getCountNumber());
        return toDto(count);
    }

    private void createVarianceJournalEntries(UUID tenantId, PhysicalCount count, List<PhysicalCountItem> items) {
        List<Account> invAccounts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "INVENTORY_ASSET");
        List<Account> gainAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "STOCK_GAIN");
        List<Account> lossAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "STOCK_LOSS");
        if (invAccounts.isEmpty()) {
            log.debug("Variance JE skipped for {}: inventory account not configured", count.getCountNumber());
            return;
        }
        Account invAccount  = invAccounts.get(0);
        Account gainAccount = gainAccounts.isEmpty() ? null : gainAccounts.get(0);
        Account lossAccount = lossAccounts.isEmpty() ? null : lossAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("PHYSICAL_COUNT");
        je.setReferenceId(count.getId());
        je.setReferenceNumber(count.getCountNumber());
        je.setDescription("Stock count variance: " + count.getCountNumber() + " — " + count.getWarehouseName());
        je.setEntryDate(count.getCountDate() != null ? count.getCountDate() : LocalDate.now());

        boolean hasLines = false;
        for (PhysicalCountItem item : items) {
            if (item.getDifferenceQuantity() == null || item.getDifferenceQuantity().compareTo(BigDecimal.ZERO) == 0) continue;
            BigDecimal unitCost = stockItemRepository
                    .findFirstByWarehouseIdAndProductId(count.getWarehouseId(), item.getProductId())
                    .map(si -> si.getAverageCost() != null ? si.getAverageCost() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            if (unitCost.compareTo(BigDecimal.ZERO) == 0) continue;
            BigDecimal varValue = item.getDifferenceQuantity().multiply(unitCost).abs();

            if (item.getDifferenceQuantity().compareTo(BigDecimal.ZERO) > 0 && gainAccount != null) {
                // Gain: DR Inventory / CR Stock Gain
                JournalEntryLine dr = new JournalEntryLine();
                dr.setAccountId(invAccount.getId()); dr.setAccountCode(invAccount.getCode()); dr.setAccountName(invAccount.getName());
                dr.setDebitAmount(varValue); dr.setCreditAmount(BigDecimal.ZERO);
                dr.setDescription("Stock gain — " + item.getProductName());
                JournalEntryLine cr = new JournalEntryLine();
                cr.setAccountId(gainAccount.getId()); cr.setAccountCode(gainAccount.getCode()); cr.setAccountName(gainAccount.getName());
                cr.setDebitAmount(BigDecimal.ZERO); cr.setCreditAmount(varValue);
                cr.setDescription("Stock gain — " + item.getProductName());
                je.getLines().add(dr); je.getLines().add(cr);
                hasLines = true;
            } else if (item.getDifferenceQuantity().compareTo(BigDecimal.ZERO) < 0 && lossAccount != null) {
                // Loss: DR Stock Loss / CR Inventory
                JournalEntryLine dr = new JournalEntryLine();
                dr.setAccountId(lossAccount.getId()); dr.setAccountCode(lossAccount.getCode()); dr.setAccountName(lossAccount.getName());
                dr.setDebitAmount(varValue); dr.setCreditAmount(BigDecimal.ZERO);
                dr.setDescription("Stock loss — " + item.getProductName());
                JournalEntryLine cr = new JournalEntryLine();
                cr.setAccountId(invAccount.getId()); cr.setAccountCode(invAccount.getCode()); cr.setAccountName(invAccount.getName());
                cr.setDebitAmount(BigDecimal.ZERO); cr.setCreditAmount(varValue);
                cr.setDescription("Stock loss — " + item.getProductName());
                je.getLines().add(dr); je.getLines().add(cr);
                hasLines = true;
            }
        }
        if (hasLines) {
            journalEntryService.create(je);
            log.info("Variance JE (DRAFT) created for count {}", count.getCountNumber());
        }
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        PhysicalCount count = physicalCountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Physical count not found: " + id));
        if ("COMPLETED".equals(count.getStatus())) {
            throw AppException.badRequest("Completed physical counts cannot be deleted");
        }
        count.setDeletedAt(LocalDateTime.now());
        physicalCountRepository.save(count);
    }

    private PhysicalCountDto toDto(PhysicalCount c) {
        PhysicalCountDto dto = new PhysicalCountDto();
        dto.setId(c.getId());
        dto.setTenantId(c.getTenantId());
        dto.setCountNumber(c.getCountNumber());
        dto.setWarehouseId(c.getWarehouseId());
        dto.setWarehouseName(c.getWarehouseName());
        dto.setCountDate(c.getCountDate());
        dto.setStatus(c.getStatus());
        dto.setNotes(c.getNotes());
        dto.setCreatedAt(c.getCreatedAt());
        if (c.getItems() != null) {
            dto.setItems(c.getItems().stream().map(item -> {
                PhysicalCountDto.ItemDto idto = new PhysicalCountDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setSystemQuantity(item.getSystemQuantity());
                idto.setCountedQuantity(item.getCountedQuantity());
                idto.setDifferenceQuantity(item.getDifferenceQuantity());
                idto.setUnit(item.getUnit());
                idto.setRemarks(item.getRemarks());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private String generateNumber() {
        return "PC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
