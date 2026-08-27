package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.inventory.dto.CreateWarehouseRequest;
import com.erp.platform.modules.inventory.dto.StockItemDto;
import com.erp.platform.modules.inventory.dto.WarehouseDto;
import com.erp.platform.modules.inventory.entity.StockItem;
import com.erp.platform.modules.inventory.entity.StockMovement;
import com.erp.platform.modules.inventory.entity.StockMovement.MovementType;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.StockItemRepository;
import com.erp.platform.modules.inventory.repository.StockTransactionRepository;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final StockTransactionRepository movementRepository;
    private final WarehouseRepository warehouseRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;
    private final ProductRepository productRepository;

    public PageResponse<WarehouseDto> listWarehouses(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(warehouseRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::warehouseToDto));
    }

    public WarehouseDto getWarehouseById(UUID id) {
        return warehouseToDto(findWarehouseOrThrow(id));
    }

    @Transactional
    public WarehouseDto createWarehouse(CreateWarehouseRequest request) {
        UUID tenantId = tenantContext.current();
        Warehouse warehouse = new Warehouse();
        warehouse.setTenantId(tenantId);
        warehouse.setName(request.getName());
        warehouse.setCode(request.getCode());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        warehouse.setContactPerson(request.getContactPerson());
        warehouse.setPhone(request.getPhone());
        warehouse.setDefault(request.isDefault());
        warehouse.setLocation(request.getLocation());
        warehouse.setActive(true);
        warehouse = warehouseRepository.save(warehouse);
        log.info("Warehouse created: id={}, name={}", warehouse.getId(), warehouse.getName());
        return warehouseToDto(warehouse);
    }

    @Transactional
    public WarehouseDto updateWarehouse(UUID id, CreateWarehouseRequest request) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setName(request.getName());
        warehouse.setCode(request.getCode());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        warehouse.setContactPerson(request.getContactPerson());
        warehouse.setPhone(request.getPhone());
        warehouse.setDefault(request.isDefault());
        warehouse.setLocation(request.getLocation());
        warehouse = warehouseRepository.save(warehouse);
        log.info("Warehouse updated: id={}", warehouse.getId());
        return warehouseToDto(warehouse);
    }

    @Transactional
    public void deleteWarehouse(UUID id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setDeletedAt(LocalDateTime.now());
        warehouseRepository.save(warehouse);
        log.info("Warehouse soft-deleted: id={}", id);
    }

    public java.util.List<StockItemDto> getLowStock() {
        UUID tenantId = tenantContext.current();
        return stockItemRepository.findLowStock(tenantId, BigDecimal.TEN)
                .stream().map(this::stockItemToDto).collect(java.util.stream.Collectors.toList());
    }

    public PageResponse<StockItemDto> listStock(UUID warehouseId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = warehouseId != null
                ? stockItemRepository.findByTenantIdAndWarehouseIdAndDeletedAtIsNull(tenantId, warehouseId, pageable)
                : stockItemRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::stockItemToDto));
    }

    public PageResponse<StockMovement> listMovements(UUID warehouseId, UUID productId,
            StockMovement.MovementType type, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Specification<StockMovement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (warehouseId != null)
                predicates.add(cb.equal(root.get("warehouseId"), warehouseId));
            if (productId != null)
                predicates.add(cb.equal(root.get("productId"), productId));
            if (type != null)
                predicates.add(cb.equal(root.get("type"), type));
            if (dateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("movementDate"), dateFrom));
            if (dateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("movementDate"), dateTo));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(movementRepository.findAll(spec, pageable));
    }

    public UUID getDefaultWarehouseId(UUID tenantId) {
        return resolveWarehouse(tenantId, null).getId();
    }

    public BigDecimal getAvailableQuantity(UUID productId) {
        UUID tenantId = tenantContext.current();
        BigDecimal qty = stockItemRepository.sumQuantityByTenantIdAndProductId(tenantId, productId);
        return qty != null ? qty : BigDecimal.ZERO;
    }

    @Transactional
    public StockItemDto adjust(UUID warehouseId, UUID productId, BigDecimal newQuantity, String reason) {
        UUID tenantId = tenantContext.current();
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);

        StockItem stock = stockItemRepository.findFirstByWarehouseIdAndProductId(warehouseId, productId)
                .orElseGet(() -> {
                    StockItem s = new StockItem();
                    s.setTenantId(tenantId);
                    s.setWarehouseId(warehouseId);
                    s.setWarehouseName(warehouse.getName());
                    s.setProductId(productId);
                    s.setProductName(resolveProductName(productId, null));
                    s.setQuantityOnHand(BigDecimal.ZERO);
                    s.setQuantityReserved(BigDecimal.ZERO);
                    s.setAverageCost(BigDecimal.ZERO);
                    return s;
                });

        if (stock.getProductName() == null) {
            stock.setProductName(resolveProductName(productId, null));
        }

        BigDecimal before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(newQuantity);
        stockItemRepository.save(stock);

        // Record movement
        StockMovement movement = new StockMovement();
        movement.setTenantId(tenantId);
        movement.setWarehouseId(warehouseId);
        movement.setProductId(productId);
        movement.setProductName(stock.getProductName());
        movement.setWarehouseName(warehouse.getName());
        movement.setType(MovementType.ADJUSTMENT);
        movement.setQuantity(newQuantity.subtract(before).abs());
        movement.setUnitCost(stock.getAverageCost());
        movement.setTotalCost(movement.getQuantity().multiply(stock.getAverageCost()).setScale(2, RoundingMode.HALF_UP));
        movement.setBalanceBefore(before);
        movement.setBalanceAfter(newQuantity);
        movement.setMovementDate(LocalDate.now());
        movement.setNotes(reason);
        movementRepository.save(movement);

        log.info("Stock adjusted: warehouse={}, product={}, before={}, after={}", warehouseId, productId, before, newQuantity);

        // Post stock variance GL entry (DRAFT)
        BigDecimal diff = newQuantity.subtract(before);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            try {
                postStockAdjustmentJournal(tenantId, stock, diff, reason);
            } catch (Exception e) {
                log.warn("Stock adjustment GL entry skipped: {}", e.getMessage());
            }
        }
        return stockItemToDto(stock);
    }

    // ── Barcode-scan goods receipt (mobile scanner) ─────────────────────────────

    /**
     * Resolve a scanned barcode / SKU / product code to the product and its live inventory position —
     * total on-hand / reserved / available plus a per-warehouse breakdown. Powers both the scan-to-receive
     * confirm step and the scan-to-view "Scan Inventory" screen on mobile / barcode-scanner devices.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> scanLookup(String code) {
        Product product = resolveScannedProduct(code);
        UUID tenantId = tenantContext.current();

        List<StockItem> stocks = stockItemRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, product.getId());
        BigDecimal onHand = BigDecimal.ZERO;
        BigDecimal reserved = BigDecimal.ZERO;
        List<Map<String, Object>> locations = new ArrayList<>();
        for (StockItem s : stocks) {
            BigDecimal qoh = s.getQuantityOnHand() != null ? s.getQuantityOnHand() : BigDecimal.ZERO;
            BigDecimal qres = s.getQuantityReserved() != null ? s.getQuantityReserved() : BigDecimal.ZERO;
            onHand = onHand.add(qoh);
            reserved = reserved.add(qres);
            Map<String, Object> loc = new LinkedHashMap<>();
            loc.put("warehouseId", s.getWarehouseId());
            loc.put("warehouseName", s.getWarehouseName());
            loc.put("location", s.getLocation());
            loc.put("quantityOnHand", qoh);
            loc.put("quantityReserved", qres);
            loc.put("quantityAvailable", qoh.subtract(qres));
            locations.add(loc);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", product.getId());
        m.put("productName", product.getName());
        m.put("code", product.getCode());
        m.put("sku", product.getSku());
        m.put("barcode", product.getBarcode());
        m.put("unit", product.getUnit());
        m.put("quantityOnHand", onHand);
        m.put("quantityReserved", reserved);
        m.put("quantityAvailable", onHand.subtract(reserved));
        m.put("locations", locations);
        return m;
    }

    /**
     * Goods receipt via barcode scan: ADDS the received quantity to on-hand at the given
     * warehouse (unlike {@link #adjust} which sets an absolute value) and records a RECEIPT movement.
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> receiveByScan(String code, BigDecimal quantity, UUID warehouseId, String reference) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw AppException.badRequest("Quantity must be greater than zero");
        }
        UUID tenantId = tenantContext.current();
        Product product = resolveScannedProduct(code);
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);

        StockItem stock = stockItemRepository.findFirstByWarehouseIdAndProductId(warehouseId, product.getId())
                .orElseGet(() -> {
                    StockItem s = new StockItem();
                    s.setTenantId(tenantId);
                    s.setWarehouseId(warehouseId);
                    s.setWarehouseName(warehouse.getName());
                    s.setProductId(product.getId());
                    s.setProductName(product.getName());
                    s.setQuantityOnHand(BigDecimal.ZERO);
                    s.setQuantityReserved(BigDecimal.ZERO);
                    s.setAverageCost(BigDecimal.ZERO);
                    return s;
                });
        if (stock.getProductName() == null) stock.setProductName(product.getName());

        BigDecimal before = stock.getQuantityOnHand() != null ? stock.getQuantityOnHand() : BigDecimal.ZERO;
        BigDecimal after  = before.add(quantity);
        stock.setQuantityOnHand(after);
        // Set before the save, so the seeded cost is actually persisted with the row.
        BigDecimal cost = costBasis(tenantId, stock, product.getId());
        stock.setAverageCost(cost);
        stockItemRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setTenantId(tenantId);
        movement.setWarehouseId(warehouseId);
        movement.setProductId(product.getId());
        movement.setProductName(stock.getProductName());
        movement.setWarehouseName(warehouse.getName());
        movement.setType(MovementType.RECEIPT);
        movement.setQuantity(quantity);
        movement.setUnitCost(cost);
        movement.setTotalCost(quantity.multiply(cost).setScale(2, RoundingMode.HALF_UP));
        movement.setBalanceBefore(before);
        movement.setBalanceAfter(after);
        movement.setMovementDate(LocalDate.now());
        movement.setNotes((reference != null && !reference.isBlank() ? "Ref " + reference + " — " : "")
                + "Barcode scan goods receipt");
        movementRepository.save(movement);

        log.info("Scan receipt: product={}, warehouse={}, +{}, onHand={}", product.getId(), warehouseId, quantity, after);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", product.getId());
        m.put("productName", stock.getProductName());
        m.put("code", product.getCode());
        m.put("barcode", product.getBarcode());
        m.put("unit", product.getUnit());
        m.put("warehouseId", warehouseId);
        m.put("warehouseName", warehouse.getName());
        m.put("received", quantity);
        m.put("quantityOnHand", after);
        return m;
    }

    private Product resolveScannedProduct(String code) {
        String c = code != null ? code.trim() : "";
        if (c.isEmpty()) throw AppException.badRequest("No barcode / code scanned");
        return productRepository.findByScanCode(tenantContext.current(), c).stream().findFirst()
                .orElseThrow(() -> AppException.notFound("No product found for scanned code: " + code));
    }

    private void postStockAdjustmentJournal(UUID tenantId, StockItem stock, BigDecimal diff, String reason) {
        java.util.List<Account> invAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "INVENTORY_ASSET");
        java.util.List<Account> varAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "STOCK_VARIANCE");
        if (invAccts.isEmpty() || varAccts.isEmpty()) {
            log.debug("Stock adjustment GL skipped: INVENTORY_ASSET or STOCK_VARIANCE account not configured");
            return;
        }
        Account invAcct = invAccts.get(0);
        Account varAcct = varAccts.get(0);
        BigDecimal unitCost = stock.getAverageCost() != null ? stock.getAverageCost() : BigDecimal.ZERO;
        BigDecimal amount = diff.abs().multiply(unitCost).setScale(2, java.math.RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) == 0) return;

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("STOCK_ADJUSTMENT");
        je.setReferenceId(stock.getId());
        je.setReferenceNumber("ADJ-" + stock.getProductId().toString().substring(0, 8).toUpperCase());
        je.setEntryDate(java.time.LocalDate.now());
        je.setDescription("Stock adjustment: " + (reason != null ? reason : "Manual") + " (diff: " + diff + ")");

        JournalEntryLine drLine = new JournalEntryLine();
        JournalEntryLine crLine = new JournalEntryLine();

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            // Stock increase: DR Inventory / CR Stock Variance Income
            drLine.setAccountId(invAcct.getId()); drLine.setAccountCode(invAcct.getCode()); drLine.setAccountName(invAcct.getName());
            drLine.setDebitAmount(amount); drLine.setCreditAmount(BigDecimal.ZERO);
            crLine.setAccountId(varAcct.getId()); crLine.setAccountCode(varAcct.getCode()); crLine.setAccountName(varAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO); crLine.setCreditAmount(amount);
        } else {
            // Stock decrease: DR Stock Variance / CR Inventory
            drLine.setAccountId(varAcct.getId()); drLine.setAccountCode(varAcct.getCode()); drLine.setAccountName(varAcct.getName());
            drLine.setDebitAmount(amount); drLine.setCreditAmount(BigDecimal.ZERO);
            crLine.setAccountId(invAcct.getId()); crLine.setAccountCode(invAcct.getCode()); crLine.setAccountName(invAcct.getName());
            crLine.setDebitAmount(BigDecimal.ZERO); crLine.setCreditAmount(amount);
        }
        drLine.setDescription("Stock adjustment — " + (reason != null ? reason : "Manual"));
        crLine.setDescription("Stock adjustment — " + (reason != null ? reason : "Manual"));
        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("Stock adjustment GL entry (DRAFT) created for product {}, diff={}, amount={}", stock.getProductId(), diff, amount);
    }

    @Transactional
    public StockItemDto addStock(UUID productId, UUID warehouseId, BigDecimal quantity,
                                  String referenceType, UUID referenceId, String referenceNumber) {
        return addStock(productId, warehouseId, quantity, referenceType, referenceId, referenceNumber, null);
    }

    @Transactional
    public StockItemDto addStock(UUID productId, UUID warehouseId, BigDecimal quantity,
                                  String referenceType, UUID referenceId, String referenceNumber,
                                  String productName) {
        return addStock(productId, warehouseId, quantity, referenceType, referenceId, referenceNumber,
                productName, "RAW", "GOOD", null, referenceType);
    }

    @Transactional
    /**
     * The rate to value a receipt at: what the item already carries, or failing that what the
     * product is bought at. Returns zero only when neither is known — an honest nil rather than
     * a guess, but the caller should expect stock valued at nothing until a price is entered.
     */
    private BigDecimal costBasis(UUID tenantId, StockItem stock, UUID productId) {
        BigDecimal existing = stock.getAverageCost();
        if (existing != null && existing.signum() > 0) return existing;
        if (productId == null) return BigDecimal.ZERO;
        return productRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, productId)
                .map(p -> p.getPurchasePrice())
                .filter(p -> p != null && p.signum() > 0)
                .orElse(BigDecimal.ZERO);
    }

    public StockItemDto addStock(UUID productId, UUID warehouseId, BigDecimal quantity,
                                  String referenceType, UUID referenceId, String referenceNumber,
                                  String productName, String materialState, String materialType,
                                  String lotNumber, String purposeType) {
        UUID tenantId = tenantContext.current();
        Warehouse warehouse = resolveWarehouse(tenantId, warehouseId);
        String resolvedState = materialState != null ? materialState : "RAW";
        String resolvedType = materialType != null ? materialType : "GOOD";

        StockItem stock = stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(
                        warehouse.getId(), productId, resolvedState, resolvedType)
                .orElseGet(() -> {
                    StockItem s = new StockItem();
                    s.setTenantId(tenantId);
                    s.setWarehouseId(warehouse.getId());
                    s.setWarehouseName(warehouse.getName());
                    s.setProductId(productId);
                    s.setMaterialState(resolvedState);
                    s.setMaterialType(resolvedType);
                    s.setQuantityOnHand(BigDecimal.ZERO);
                    s.setQuantityReserved(BigDecimal.ZERO);
                    s.setAverageCost(BigDecimal.ZERO);
                    return s;
                });

        if (stock.getProductName() == null) {
            stock.setProductName(resolveProductName(productId, productName));
        }
        if (stock.getWarehouseName() == null) {
            stock.setWarehouseName(warehouse.getName());
        }

        BigDecimal before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(before.add(quantity));
        // Average cost starts at zero and nothing ever raised it, so every movement recorded a rate
        // of nil and every stock valuation came out at ₹0. Seed it from the product's purchase price
        // the first time goods arrive, so the figure carried forward is a real one.
        BigDecimal cost = costBasis(tenantId, stock, productId);
        stock.setAverageCost(cost);
        stockItemRepository.save(stock);

        recordMovement(tenantId, warehouse.getId(), productId, MovementType.RECEIPT,
                referenceType, referenceId, referenceNumber,
                quantity, cost, before, stock.getQuantityOnHand(),
                stock.getProductName(), warehouse.getName(),
                lotNumber, purposeType, resolvedState, resolvedType);

        return stockItemToDto(stock);
    }

    @Transactional
    public StockItemDto deductStock(UUID productId, UUID warehouseId, BigDecimal quantity,
                                     String referenceType, UUID referenceId, String referenceNumber) {
        return deductStock(productId, warehouseId, quantity, referenceType, referenceId, referenceNumber,
                "RAW", "GOOD", null, referenceType);
    }

    @Transactional
    public StockItemDto deductStock(UUID productId, UUID warehouseId, BigDecimal quantity,
                                     String referenceType, UUID referenceId, String referenceNumber,
                                     String materialState, String materialType,
                                     String lotNumber, String purposeType) {
        UUID tenantId = tenantContext.current();
        Warehouse warehouse = resolveWarehouse(tenantId, warehouseId);
        String resolvedState = materialState != null ? materialState : "RAW";
        String resolvedType = materialType != null ? materialType : "GOOD";

        // Exact dimension match first; fall back to any bucket for this warehouse+product so
        // deduction isn't blocked when the caller's default dimension differs from how the
        // stock was originally tagged (e.g. dispatch deducting finished goods added as PACKED).
        StockItem stock = stockItemRepository.findByWarehouseIdAndProductIdAndMaterialStateAndMaterialType(
                        warehouse.getId(), productId, resolvedState, resolvedType)
                .or(() -> stockItemRepository.findFirstByWarehouseIdAndProductId(warehouse.getId(), productId))
                .orElseThrow(() -> AppException.badRequest("No stock found for product: " + productId));

        if (stock.getQuantityOnHand().compareTo(quantity) < 0) {
            throw AppException.insufficientStock("Insufficient stock. Available: " + stock.getQuantityOnHand()
                    + ", Requested: " + quantity);
        }

        BigDecimal before = stock.getQuantityOnHand();
        stock.setQuantityOnHand(before.subtract(quantity));
        stockItemRepository.save(stock);

        recordMovement(tenantId, warehouse.getId(), productId, MovementType.ISSUE,
                referenceType, referenceId, referenceNumber,
                quantity, stock.getAverageCost(), before, stock.getQuantityOnHand(),
                stock.getProductName(), warehouse.getName(),
                lotNumber, purposeType, resolvedState, resolvedType);

        BigDecimal avgCost = stock.getAverageCost() != null ? stock.getAverageCost() : BigDecimal.ZERO;
        if (avgCost.compareTo(BigDecimal.ZERO) > 0) {
            try {
                postStockDeductionJournal(tenantId, productId, quantity, avgCost,
                        referenceType, referenceId, referenceNumber);
            } catch (Exception e) {
                log.warn("COGS GL entry skipped for product {}: {}", productId, e.getMessage());
            }
        }

        return stockItemToDto(stock);
    }

    private void postStockDeductionJournal(UUID tenantId, UUID productId, BigDecimal quantity,
            BigDecimal unitCost, String referenceType, UUID referenceId, String referenceNumber) {
        java.util.List<Account> cogsAccts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "COGS");
        java.util.List<Account> invAccts  = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "INVENTORY_ASSET");
        if (cogsAccts.isEmpty() || invAccts.isEmpty()) {
            log.debug("COGS GL skipped for product {}: COGS or INVENTORY_ASSET account not configured", productId);
            return;
        }
        BigDecimal amount = quantity.multiply(unitCost).setScale(2, java.math.RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) == 0) return;

        Account cogsAcct = cogsAccts.get(0);
        Account invAcct  = invAccts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType(referenceType);
        je.setReferenceId(referenceId);
        je.setReferenceNumber(referenceNumber);
        je.setEntryDate(java.time.LocalDate.now());
        je.setDescription("COGS: " + referenceType + " " + referenceNumber);

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(cogsAcct.getId()); drLine.setAccountCode(cogsAcct.getCode()); drLine.setAccountName(cogsAcct.getName());
        drLine.setDebitAmount(amount); drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("Cost of goods — " + referenceNumber);

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(invAcct.getId()); crLine.setAccountCode(invAcct.getCode()); crLine.setAccountName(invAcct.getName());
        crLine.setDebitAmount(BigDecimal.ZERO); crLine.setCreditAmount(amount);
        crLine.setDescription("Inventory reduction — " + referenceNumber);

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("COGS GL entry (DRAFT) created for {} {}, qty={}, amount={}", referenceType, referenceNumber, quantity, amount);
    }

    private Warehouse resolveWarehouse(UUID tenantId, UUID warehouseId) {
        if (warehouseId != null) {
            return warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, warehouseId)
                    .orElseThrow(() -> AppException.notFound("Warehouse not found: " + warehouseId));
        }
        return warehouseRepository.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(tenantId)
                .orElseGet(() -> warehouseRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().findFirst()
                        .orElseThrow(() -> AppException.notFound("No warehouse found for tenant")));
    }

    private void recordMovement(UUID tenantId, UUID warehouseId, UUID productId,
            MovementType type, String refType, UUID refId, String refNumber,
            BigDecimal qty, BigDecimal unitCost, BigDecimal before, BigDecimal after,
            String productName, String warehouseName) {
        recordMovement(tenantId, warehouseId, productId, type, refType, refId, refNumber,
                qty, unitCost, before, after, productName, warehouseName,
                null, refType, null, null);
    }

    private void recordMovement(UUID tenantId, UUID warehouseId, UUID productId,
            MovementType type, String refType, UUID refId, String refNumber,
            BigDecimal qty, BigDecimal unitCost, BigDecimal before, BigDecimal after,
            String productName, String warehouseName,
            String lotNumber, String purposeType, String materialState, String materialType) {
        StockMovement movement = new StockMovement();
        movement.setTenantId(tenantId);
        movement.setWarehouseId(warehouseId);
        movement.setProductId(productId);
        movement.setProductName(productName);
        movement.setWarehouseName(warehouseName);
        movement.setType(type);
        movement.setReferenceType(refType);
        movement.setReferenceId(refId);
        movement.setReferenceNumber(refNumber);
        movement.setQuantity(qty);
        movement.setUnitCost(unitCost);
        movement.setTotalCost(qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP));
        movement.setBalanceBefore(before);
        movement.setBalanceAfter(after);
        movement.setMovementDate(LocalDate.now());
        movement.setLotNumber(lotNumber);
        movement.setPurposeType(purposeType);
        movement.setMaterialState(materialState);
        movement.setMaterialType(materialType);
        movementRepository.save(movement);
    }

    public Map<String, Object> getStockLedger(UUID productId, UUID warehouseId, LocalDate from, LocalDate to) {
        UUID tenantId = tenantContext.current();

        String productName = productRepository.findById(productId)
                .map(p -> p.getName()).orElse("Unknown Product");

        String warehouseName = null;
        if (warehouseId != null) {
            warehouseName = warehouseRepository.findById(warehouseId)
                    .map(w -> w.getName()).orElse(null);
        }

        // Opening balance: last movement before `from`
        var openingPage = org.springframework.data.domain.PageRequest.of(0, 1);
        List<StockMovement> openingMovements = warehouseId != null
                ? movementRepository.findLastMovementBeforeByWarehouse(tenantId, productId, warehouseId, from, openingPage)
                : movementRepository.findLastMovementBefore(tenantId, productId, from, openingPage);

        BigDecimal openingQty = openingMovements.isEmpty() ? BigDecimal.ZERO
                : openingMovements.get(0).getBalanceAfter();

        // Opening value: opening qty × average cost from StockItem (best approximation)
        BigDecimal avgCost = BigDecimal.ZERO;
        if (warehouseId != null) {
            var si = stockItemRepository.findFirstByWarehouseIdAndProductId(warehouseId, productId);
            if (si.isPresent()) avgCost = si.get().getAverageCost() != null ? si.get().getAverageCost() : BigDecimal.ZERO;
        } else {
            // Use first StockItem average cost found
            var page = stockItemRepository.findByTenantIdAndDeletedAtIsNull(tenantId,
                    org.springframework.data.domain.PageRequest.of(0, 1));
            // find by productId
            var allSi = stockItemRepository.findByTenantIdAndDeletedAtIsNull(tenantId,
                    org.springframework.data.domain.Pageable.unpaged());
            for (StockItem si : allSi) {
                if (productId.equals(si.getProductId()) && si.getAverageCost() != null) {
                    avgCost = si.getAverageCost();
                    break;
                }
            }
        }
        BigDecimal openingValue = openingQty.multiply(avgCost).setScale(2, RoundingMode.HALF_UP);

        // Movements in period
        Specification<StockMovement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("productId"), productId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (warehouseId != null) predicates.add(cb.equal(root.get("warehouseId"), warehouseId));
            predicates.add(cb.greaterThanOrEqualTo(root.get("movementDate"), from));
            predicates.add(cb.lessThanOrEqualTo(root.get("movementDate"), to));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<StockMovement> movements = movementRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.ASC, "movementDate",
                        "createdAt"));

        BigDecimal totalIn = BigDecimal.ZERO, totalOut = BigDecimal.ZERO;
        BigDecimal totalInValue = BigDecimal.ZERO, totalOutValue = BigDecimal.ZERO;
        List<Map<String, Object>> lines = new ArrayList<>();

        for (StockMovement m : movements) {
            boolean isIn = m.getType() == MovementType.RECEIPT
                    || m.getType() == MovementType.TRANSFER_IN
                    || (m.getType() == MovementType.ADJUSTMENT
                        && m.getBalanceAfter() != null && m.getBalanceBefore() != null
                        && m.getBalanceAfter().compareTo(m.getBalanceBefore()) >= 0);

            BigDecimal qty = m.getQuantity() != null ? m.getQuantity() : BigDecimal.ZERO;
            BigDecimal value = m.getTotalCost() != null ? m.getTotalCost() : BigDecimal.ZERO;

            if (isIn) { totalIn = totalIn.add(qty); totalInValue = totalInValue.add(value); }
            else       { totalOut = totalOut.add(qty); totalOutValue = totalOutValue.add(value); }

            Map<String, Object> line = new LinkedHashMap<>();
            line.put("date",            m.getMovementDate());
            line.put("type",            m.getType() != null ? m.getType().name() : "");
            line.put("referenceType",   m.getReferenceType() != null ? m.getReferenceType() : "");
            line.put("referenceNumber", m.getReferenceNumber() != null ? m.getReferenceNumber() : "");
            line.put("warehouseName",   m.getWarehouseName() != null ? m.getWarehouseName() : "");
            line.put("qtyIn",           isIn ? qty : BigDecimal.ZERO);
            line.put("qtyOut",          isIn ? BigDecimal.ZERO : qty);
            line.put("unitCost",        m.getUnitCost() != null ? m.getUnitCost() : BigDecimal.ZERO);
            line.put("lineValue",       value);
            line.put("balance",         m.getBalanceAfter() != null ? m.getBalanceAfter() : BigDecimal.ZERO);
            line.put("notes",           m.getNotes() != null ? m.getNotes() : "");
            lines.add(line);
        }

        BigDecimal closingQty = lines.isEmpty() ? openingQty
                : (BigDecimal) lines.get(lines.size() - 1).get("balance");
        BigDecimal closingValue = closingQty.multiply(avgCost).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId",     productId);
        result.put("productName",   productName);
        result.put("warehouseId",   warehouseId);
        result.put("warehouseName", warehouseName);
        result.put("from",          from);
        result.put("to",            to);
        result.put("openingQty",    openingQty);
        result.put("openingValue",  openingValue);
        result.put("lines",         lines);
        result.put("closingQty",    closingQty);
        result.put("closingValue",  closingValue);
        result.put("totalIn",       totalIn);
        result.put("totalOut",      totalOut);
        result.put("totalInValue",  totalInValue);
        result.put("totalOutValue", totalOutValue);
        return result;
    }

    private String resolveProductName(UUID productId, String supplied) {
        if (supplied != null && !supplied.isBlank()) return supplied;
        return productRepository.findById(productId)
                .map(p -> p.getName())
                .orElse(null);
    }

    private Warehouse findWarehouseOrThrow(UUID id) {
        return warehouseRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Warehouse not found: " + id));
    }

    private WarehouseDto warehouseToDto(Warehouse w) {
        WarehouseDto dto = new WarehouseDto();
        dto.setId(w.getId());
        dto.setTenantId(w.getTenantId());
        dto.setName(w.getName());
        dto.setCode(w.getCode());
        dto.setAddress(w.getAddress());
        dto.setCity(w.getCity());
        dto.setContactPerson(w.getContactPerson());
        dto.setPhone(w.getPhone());
        dto.setActive(w.isActive());
        dto.setDefault(w.isDefault());
        dto.setLocation(w.getLocation());
        dto.setCreatedAt(w.getCreatedAt());
        return dto;
    }

    public List<String> listLocations() {
        return warehouseRepository.findDistinctLocationsByTenantId(tenantContext.current());
    }

    public List<WarehouseDto> listWarehousesByLocation(String location) {
        UUID tenantId = tenantContext.current();
        return warehouseRepository.findByTenantIdAndActiveTrueAndLocationAndDeletedAtIsNull(tenantId, location)
                .stream().map(this::warehouseToDto).collect(java.util.stream.Collectors.toList());
    }

    private StockItemDto stockItemToDto(StockItem s) {
        StockItemDto dto = new StockItemDto();
        dto.setId(s.getId());
        dto.setTenantId(s.getTenantId());
        dto.setWarehouseId(s.getWarehouseId());
        dto.setWarehouseName(s.getWarehouseName());
        dto.setProductId(s.getProductId());
        String name = s.getProductName();
        if (name == null && s.getProductId() != null) {
            name = resolveProductName(s.getProductId(), null);
        }
        dto.setProductName(name);
        dto.setQuantityOnHand(s.getQuantityOnHand());
        dto.setQuantityReserved(s.getQuantityReserved());
        dto.setQuantityAvailable(s.getQuantityAvailable());
        dto.setAverageCost(s.getAverageCost());
        dto.setLocation(s.getLocation());
        dto.setMaterialState(s.getMaterialState());
        dto.setMaterialType(s.getMaterialType());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
