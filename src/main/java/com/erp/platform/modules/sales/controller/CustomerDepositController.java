package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.CustomerDeposit;
import com.erp.platform.modules.sales.repository.CustomerDepositRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/customer-deposits")
@RequiredArgsConstructor
@Tag(name = "Customer Deposits", description = "Customer security deposit management")
public class CustomerDepositController {

    private final CustomerDepositRepository repo;
    /** Deposits store the customer's name, but not every writer supplied one — the id can. */
    private final com.erp.platform.modules.master.repository.CustomerRepository customerRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customer deposits")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result.map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create customer deposit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        CustomerDeposit d = new CustomerDeposit();
        d.setTenantId(tenantId);
        d.setDepositNumber(generateNumber());
        if (req.get("customerId") != null) d.setCustomerId(UUID.fromString(req.get("customerId").toString()));
        d.setCustomerName((String) req.get("customerName"));
        String dateStr = (String) req.get("paymentDate");
        d.setPaymentDate(dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now());
        d.setPaymentMode((String) req.get("paymentMode"));
        d.setReferenceNumber((String) req.get("referenceNumber"));
        d.setNotes((String) req.get("notes"));
        BigDecimal amt = req.get("amount") != null ? new BigDecimal(req.get("amount").toString()) : BigDecimal.ZERO;
        d.setAmount(amt);
        d.setAmountAvailable(amt);
        d.setStatus(req.getOrDefault("status", "AVAILABLE").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(d))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update customer deposit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        CustomerDeposit d = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Customer deposit not found: " + id));
        if (req.containsKey("customerId") && req.get("customerId") != null)
            d.setCustomerId(UUID.fromString(req.get("customerId").toString()));
        if (req.containsKey("customerName")) d.setCustomerName((String) req.get("customerName"));
        if (req.containsKey("paymentDate") && req.get("paymentDate") != null) {
            String ds = req.get("paymentDate").toString();
            if (!ds.isBlank()) d.setPaymentDate(LocalDate.parse(ds));
        }
        if (req.containsKey("paymentMode")) d.setPaymentMode((String) req.get("paymentMode"));
        if (req.containsKey("referenceNumber")) d.setReferenceNumber((String) req.get("referenceNumber"));
        if (req.containsKey("notes")) d.setNotes((String) req.get("notes"));
        if (req.containsKey("amount") && req.get("amount") != null) {
            BigDecimal amt = new BigDecimal(req.get("amount").toString());
            d.setAmount(amt);
            d.setAmountAvailable(amt);
        }
        if (req.containsKey("status")) d.setStatus(req.get("status").toString());
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(d))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete customer deposit")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        CustomerDeposit d = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Customer deposit not found: " + id));
        d.setDeletedAt(LocalDateTime.now());
        repo.save(d);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String generateNumber() {
        return "DEP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String resolveCustomerName(CustomerDeposit d) {
        if (d.getCustomerName() != null && !d.getCustomerName().isBlank()) return d.getCustomerName();
        if (d.getCustomerId() == null) return "";
        return customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(d.getTenantId(), d.getCustomerId())
                .map(c -> c.getName()).orElse("");
    }

    private Map<String, Object> toMap(CustomerDeposit d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("depositNumber", d.getDepositNumber());
        m.put("customerId", d.getCustomerId());
        // The grid showed a blank column for every deposit saved with only a customer id. The
        // name is denormalised for speed, so when it is missing fall back to the customer record
        // rather than showing nothing — a deposit nobody can attribute is not much use.
        m.put("customerName", resolveCustomerName(d));
        m.put("paymentDate", d.getPaymentDate() == null ? "" : d.getPaymentDate().toString());
        m.put("paymentMode", d.getPaymentMode() == null ? "" : d.getPaymentMode());
        m.put("referenceNumber", d.getReferenceNumber() == null ? "" : d.getReferenceNumber());
        m.put("amount", d.getAmount());
        m.put("amountAvailable", d.getAmountAvailable());
        m.put("status", d.getStatus() == null ? "AVAILABLE" : d.getStatus());
        m.put("notes", d.getNotes() == null ? "" : d.getNotes());
        m.put("createdAt", d.getCreatedAt() == null ? "" : d.getCreatedAt().toString());
        return m;
    }
}
