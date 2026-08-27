package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.ExchangeRate;
import com.erp.platform.modules.accounting.service.ExchangeRateService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Accounting - Exchange Rates", description = "Foreign currency exchange rate management")
public class ExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List exchange rates")
    public ResponseEntity<ApiResponse<PageResponse<ExchangeRate>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "effectiveDate"));
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExchangeRate>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/rate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get exchange rate for a currency pair as of a date")
    public ResponseEntity<ApiResponse<BigDecimal>> getRate(
            @RequestParam String base,
            @RequestParam String target,
            @RequestParam(required = false) String asOf) {
        LocalDate date = asOf != null ? LocalDate.parse(asOf) : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(service.getRate(base, target, date)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create exchange rate")
    public ResponseEntity<ApiResponse<ExchangeRate>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(body), "Exchange rate created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ExchangeRate>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
