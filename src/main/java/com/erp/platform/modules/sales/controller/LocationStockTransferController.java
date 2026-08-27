package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.LocationStockTransfer;
import com.erp.platform.modules.sales.repository.LocationStockTransferRepository;
import com.erp.platform.modules.sales.service.LocationTransferPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.erp.platform.modules.sales.entity.CustomerTransferLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/location-stock-transfers")
@RequiredArgsConstructor
@Tag(name = "Sales - Location Stock Transfers", description = "Location-to-location lot-based stock transfers")
public class LocationStockTransferController {

    private final LocationStockTransferRepository repo;
    private final LocationTransferPostingService postingService;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List location stock transfers")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create location stock transfer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        LocationStockTransfer t = new LocationStockTransfer();
        t.setTenantId(tenantId);
        t.setTransferNumber(str(req, "transferNumber") != null && !str(req, "transferNumber").isBlank()
                ? str(req, "transferNumber") : generateNumber(tenantId));
        apply(t, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(t))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update location stock transfer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        LocationStockTransfer t = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        if (t.isPosted()) throw AppException.badRequest("A posted transfer cannot be edited");
        apply(t, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(t))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete location stock transfer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        LocationStockTransfer t = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Stock transfer not found: " + id));
        t.setDeletedAt(LocalDateTime.now());
        repo.save(t);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post the transfer: move the lot's stock from the source to the destination location")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(toMap(postingService.post(id)),
                "Transfer posted: stock moved between locations"));
    }

    private void apply(LocationStockTransfer t, Map<String, Object> req) {
        if (req.containsKey("transferNumber") && str(req, "transferNumber") != null && !str(req, "transferNumber").isBlank())
            t.setTransferNumber(str(req, "transferNumber"));
        if (req.containsKey("transferDate")) t.setTransferDate(date(req, "transferDate"));
        if (req.containsKey("fromLocation")) t.setFromLocation(str(req, "fromLocation"));
        if (req.containsKey("toLocation")) t.setToLocation(str(req, "toLocation"));
        if (req.containsKey("productName")) t.setProductName(str(req, "productName"));
        if (req.containsKey("lotNumber")) t.setLotNumber(str(req, "lotNumber"));
        if (req.containsKey("quantity")) t.setQuantity(decimal(req, "quantity"));
        if (req.containsKey("freightTotal")) t.setFreightTotal(decimal(req, "freightTotal"));
        if (req.containsKey("freightPaid")) t.setFreightPaid(decimal(req, "freightPaid"));
        if (req.containsKey("description")) t.setDescription(str(req, "description"));
        if (req.containsKey("status")) t.setStatus(str(req, "status"));
        if (req.containsKey("freightToPay")) t.setFreightToPay(decimal(req, "freightToPay"));
        else t.setFreightToPay(nz(t.getFreightTotal()).subtract(nz(t.getFreightPaid())));
        // from-location address
        if (req.containsKey("fromAddress1")) t.setFromAddress1(str(req, "fromAddress1"));
        if (req.containsKey("fromAddress2")) t.setFromAddress2(str(req, "fromAddress2"));
        if (req.containsKey("fromState")) t.setFromState(str(req, "fromState"));
        if (req.containsKey("fromDistrict")) t.setFromDistrict(str(req, "fromDistrict"));
        if (req.containsKey("toDistrict")) t.setToDistrict(str(req, "toDistrict"));
        if (req.containsKey("fromCity")) t.setFromCity(str(req, "fromCity"));
        if (req.containsKey("fromZip")) t.setFromZip(str(req, "fromZip"));
        if (req.containsKey("fromPhone")) t.setFromPhone(str(req, "fromPhone"));
        // to-location address
        if (req.containsKey("toAddress1")) t.setToAddress1(str(req, "toAddress1"));
        if (req.containsKey("toAddress2")) t.setToAddress2(str(req, "toAddress2"));
        if (req.containsKey("toState")) t.setToState(str(req, "toState"));
        if (req.containsKey("toCity")) t.setToCity(str(req, "toCity"));
        if (req.containsKey("toZip")) t.setToZip(str(req, "toZip"));
        if (req.containsKey("toPhone")) t.setToPhone(str(req, "toPhone"));
        // line items
        Object itemsObj = req.get("items");
        if (itemsObj instanceof List<?> list) {
            t.getItems().clear();
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> raw)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) raw;
                CustomerTransferLine l = new CustomerTransferLine();
                l.setCropGroup(str(m, "cropGroup"));
                l.setCrop(str(m, "crop"));
                l.setVariety(str(m, "variety"));
                l.setCropVariety(str(m, "cropVariety"));
                l.setProductId(str(m, "productId"));
                l.setProductName(str(m, "productName"));
                l.setLotNumber(str(m, "lotNumber"));
                l.setPacks(decimal(m, "packs"));
                l.setPacksDamaged(decimal(m, "packsDamaged"));
                l.setFromUnitPrice(decimal(m, "fromUnitPrice"));
                l.setFromDiscount(decimal(m, "fromDiscount"));
                l.setFromStCst(decimal(m, "fromStCst"));
                l.setToUnitPrice(decimal(m, "toUnitPrice"));
                l.setToDiscount(decimal(m, "toDiscount"));
                l.setToStCst(decimal(m, "toStCst"));
                BigDecimal fromPrice = nz(l.getPacks()).multiply(nz(l.getFromUnitPrice())).subtract(nz(l.getFromDiscount())).subtract(nz(l.getFromStCst()));
                BigDecimal toPrice = nz(l.getPacks()).multiply(nz(l.getToUnitPrice())).subtract(nz(l.getToDiscount())).subtract(nz(l.getToStCst()));
                l.setFromCustPrice(fromPrice);
                l.setToCustPrice(toPrice);
                l.setFromCustomerAmount(fromPrice);
                l.setToCustomerAmount(toPrice);
                t.getItems().add(l);
            }
            if (!t.getItems().isEmpty()) {
                CustomerTransferLine first = t.getItems().get(0);
                t.setLotNumber(first.getLotNumber());
                t.setProductName(first.getProductName());
                if (t.getQuantity() == null) t.setQuantity(first.getPacks());
            }
        }
        if (t.getTransferDate() == null) t.setTransferDate(LocalDate.now());
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String generateNumber(UUID tenantId) {
        long n = repo.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("STR-%d-%05d", LocalDate.now().getYear(), n);
    }

    private static String str(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? null : v.toString();
    }

    private static BigDecimal decimal(Map<String, Object> r, String k) {
        String s = str(r, k);
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static LocalDate date(Map<String, Object> r, String k) {
        String s = str(r, k);
        return (s == null || s.isBlank()) ? null : LocalDate.parse(s.substring(0, 10));
    }

    private Map<String, Object> toMap(LocationStockTransfer t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("transferNumber", t.getTransferNumber());
        m.put("transferDate", t.getTransferDate() == null ? "" : t.getTransferDate().toString());
        m.put("fromLocation", t.getFromLocation() == null ? "" : t.getFromLocation());
        m.put("toLocation", t.getToLocation() == null ? "" : t.getToLocation());
        m.put("productName", t.getProductName() == null ? "" : t.getProductName());
        m.put("lotNumber", t.getLotNumber() == null ? "" : t.getLotNumber());
        m.put("quantity", t.getQuantity());
        m.put("freightTotal", t.getFreightTotal());
        m.put("freightPaid", t.getFreightPaid());
        m.put("description", t.getDescription() == null ? "" : t.getDescription());
        m.put("freightToPay", t.getFreightToPay());
        m.put("fromAddress1", t.getFromAddress1()); m.put("fromAddress2", t.getFromAddress2());
        m.put("fromState", t.getFromState()); m.put("fromDistrict", t.getFromDistrict()); m.put("fromCity", t.getFromCity());
        m.put("toDistrict", t.getToDistrict());
        m.put("fromZip", t.getFromZip()); m.put("fromPhone", t.getFromPhone());
        m.put("toAddress1", t.getToAddress1()); m.put("toAddress2", t.getToAddress2());
        m.put("toState", t.getToState()); m.put("toCity", t.getToCity());
        m.put("toZip", t.getToZip()); m.put("toPhone", t.getToPhone());
        m.put("status", t.getStatus());
        m.put("posted", t.isPosted());
        List<Map<String, Object>> items = new ArrayList<>();
        if (t.getItems() != null) {
            for (CustomerTransferLine l : t.getItems()) {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("cropGroup", l.getCropGroup()); im.put("crop", l.getCrop());
                im.put("variety", l.getVariety()); im.put("cropVariety", l.getCropVariety());
                im.put("productId", l.getProductId()); im.put("productName", l.getProductName());
                im.put("lotNumber", l.getLotNumber());
                im.put("packs", l.getPacks()); im.put("packsDamaged", l.getPacksDamaged());
                im.put("fromUnitPrice", l.getFromUnitPrice()); im.put("fromDiscount", l.getFromDiscount());
                im.put("fromStCst", l.getFromStCst()); im.put("fromCustPrice", l.getFromCustPrice());
                im.put("toUnitPrice", l.getToUnitPrice()); im.put("toDiscount", l.getToDiscount());
                im.put("toStCst", l.getToStCst()); im.put("toCustPrice", l.getToCustPrice());
                im.put("fromCustomerAmount", l.getFromCustomerAmount()); im.put("toCustomerAmount", l.getToCustomerAmount());
                items.add(im);
            }
        }
        m.put("items", items);
        return m;
    }
}
